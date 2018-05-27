# gpstager

This is a Spring Cloud Task that leverages the powerful parallel bulk file loading features from Greenplum Database.  Why circle the earth looking for ways to piece together things like Kafka, Cassandra, MongoDB, Spark, etc when you can simply use PCF for processing and GPDB for bulk operations?  Not to mention the super simple development model provide by Spring!

Results: Using a single node GPDB vm instance, you can bulk load 1,051,080 rows in 40s where batch processes are run as CF tasks via Spring Cloud Dataflow wihtout spending any money on super mega infrastracture ;-).  Tests were performed using a vsphere env with 2 very old Dell R900 hosts, 1 Gig network, Greenplum DB + Pivotal Cloud Foundry all deployed on the same infra.  Wonder if we can improve that 40s?... hmmm.  From here you can do further data manipulation or even split the rows into individual events feeding into Streams.

In order to use this Task application with Spring Cloud Dataflow on PCF, you will need:

1. A PCF env with Spring Cloud Dataflow for PCF and the Greenplum DB broker deployed. The broker code is found here:  https://github.com/Pivotal-Field-Engineering/gpdb-broker

Note: You will need to make minor changes to this code so that a) The connect string generated from the CF binding process defaults to sslmode=disable if your GPDB instance doesn't have SSL turned on and b) DB user created has permission CREATEEXTTABLE.

2. Single node GPDB 5.1.4 vm - download OVA from https://network.pivotal.io/products/pivotal-gpdb#/releases/35017

This VM needs to be network accessible from PCF.
This VM does include tutorial material which can be used as the basis to try this out.  Familiarize yourself with this tutorial: http://greenplum.org/gpdb-sandbox-tutorials/

Make sure you setup the VM so that at least one GPFDIST process is running where it can access the FAA airline data.


Once your test env is setup properly:

1. clone this repo
2. ./mvnw clean package -DskipTests=true (todo: implement tests)
3. Publish the jarfile artifact.  SCDF supports registering Stream and Task applications from maven repository or via http resource.  If you don't have a maven repo you can use PCF to host your jarfile and access via http.  To do so:

    mkdir gpstager-artifact; cd gpstager-artifact
    cp ../target/gpstager-0.0.1-SNAPSHOT.jar .
    cf push gpstager-jar -b staticfile_buildpack -p .
    Note the URL.  Example: https://gpstager-jar.cf.wrightcode.net/gpstager-0.0.1-SNAPSHOT.jar

4. cf login
5. cf create-service p-dataflow  standard df-server
6. cf create-service Greenplum Free batch-processing-db
7. From the PCF Apps Manager GUI Navigate to your SCDF service instance, click the 'Manage' link
   Note: All SCDF steps can be done from commandline, use GUI for easier understanding
8. Register the Task Application using the http url from step #3, name it 'gpstager'
9. Using the Composed Task Editor, create composed task and paste the following in the text definition field

```gpstager --gpf-dist-server-list=<ip address for GPDB vm>:8081 --relative-file-paths=/otp*.gz --gpdb-host=<ip address for GPDB vm> --ext-table-name=test_ext --file-format=CSV --dim-table-name=test_staging --attr-list='Flt_Year , Flt_Quarter , Flt_Month , Flt_DayofMonth , Flt_DayOfWeek , Flight , UniqueCarrier , AirlineID , Carrier , TailNum , FlightNum , Origin , OriginCityName , OriginState , OriginStateFips , OriginStateName , OriginWac , Dest , DestCityName , DestState , DestStateFips , DestStateName , DestWac , CRSDepTime , DepTime , DepDelay , DepDelayMinutes , DepDel15 , DepartureDelayGroups , DepTimeBlk , TaxiOut , WheelsOff , WheelsOn , TaxiIn , CRSArrTime , ArrTime , ArrDelay , ArrDelayMinutes , ArrDel15 , ArrivalDelayGroups , ArrTimeBlk , Cancelled , CancellationCode , Diverted , CRSElapsedTime , ActualElapsedTime , AirTime , Flights , Distance , DistanceGroup , CarrierDelay , WeatherDelay , NASDelay , SecurityDelay , LateAircraftDelay , FirstDepTime , TotalAddGTime , LongestAddGTime , DivAirportLandings , DivReachedDest , DivActualElapsedTime , DivArrDelay , DivDistance , Div1Airport , Div1WheelsOn , Div1TotalGTime , Div1LongestGTime , Div1WheelsOff , Div1TailNum , Div2Airport , Div2WheelsOn , Div2TotalGTime , Div2LongestGTime , Div2WheelsOff , Div2TailNum , Div3Airport , Div3WheelsOn , Div3TotalGTime , Div3LongestGTime , Div3WheelsOff , Div3TailNum , Div4Airport , Div4WheelsOn , Div4TotalGTime , Div4LongestGTime , Div4WheelsOff , Div4TailNum , Div5Airport , Div5WheelsOn , Div5TotalGTime , Div5LongestGTime , Div5WheelsOff , Div5TailNum , trailer ' --delimiter=,```

10. Launch the Task adding the following additional property setting:  deployer.gpstager.cloudfoundry.services=batch-processing-db


