using System;
using System.Collections.Generic;
using System.Text;
/* keys_subscriber.cs

   A subscription example

   This file is derived from code automatically generated by the rtiddsgen 
   command:

   rtiddsgen -language C# -example <arch> keys.idl

   Example subscription of type keys automatically generated by 
   'rtiddsgen'. To test them, follow these steps:

   (1) Compile this file and the example publication.

   (2) Start the subscription with the command
       objs\<arch>\keys_subscriber <domain_id> <sample_count>

   (3) Start the publication with the command
       objs\<arch>\keys_publisher <domain_id> <sample_count>

   (4) [Optional] Specify the list of discovery initial peers and 
       multicast receive addresses via an environment variable or a file 
       (in the current working directory) called NDDS_DISCOVERY_PEERS. 

   You can run any number of publishers and subscribers programs, and can 
   add and remove them dynamically from the domain.
                                   
   Example:
        
       To run the example application on domain <domain_id>:
                          
       bin\<Debug|Release>\keys_publisher <domain_id> <sample_count>  
       bin\<Debug|Release>\keys_subscriber <domain_id> <sample_count>
              
       
modification history
------------ -------
*/

public class keysSubscriber {

    public class keysListener : DDS.DataReaderListener {

        public override void on_requested_deadline_missed(
            DDS.DataReader reader,
            ref DDS.RequestedDeadlineMissedStatus status) {}
    
        public override void on_requested_incompatible_qos(
            DDS.DataReader reader,
            DDS.RequestedIncompatibleQosStatus status) {}
    
        public override void on_sample_rejected(
            DDS.DataReader reader,
            ref DDS.SampleRejectedStatus status) {}

        public override void on_liveliness_changed(
            DDS.DataReader reader,
            ref DDS.LivelinessChangedStatus status) {}

        public override void on_sample_lost(
            DDS.DataReader reader,
            ref DDS.SampleLostStatus status) {}

        public override void on_subscription_matched(
            DDS.DataReader reader,
            ref DDS.SubscriptionMatchedStatus status) {}

        public override void on_data_available(DDS.DataReader reader) {
            keysDataReader keys_reader =
                (keysDataReader)reader;
            
            try {
                keys_reader.take(
                    data_seq,
                    info_seq,
                    DDS.ResourceLimitsQosPolicy.LENGTH_UNLIMITED,
                    DDS.SampleStateKind.ANY_SAMPLE_STATE,
                    DDS.ViewStateKind.ANY_VIEW_STATE,
                    DDS.InstanceStateKind.ANY_INSTANCE_STATE);
            }
            catch(DDS.Retcode_NoData) {
                return;
            }
            catch(DDS.Exception e) {
                Console.WriteLine("take error {0}", e);
                return;
            }

            System.Int32 data_length = data_seq.length;
            for (int i = 0; i < data_length; ++i)
            {
                /* Start changes for Keyed_Data */
                /* We first check if the sample includes valid data */
                if (info_seq.get_at(i).valid_data)
                {
                    if (info_seq.get_at(i).view_state ==
                        DDS.ViewStateKind.NEW_VIEW_STATE)
                    {
                        Console.WriteLine("Found new instance; code = {0}",
                            data_seq.get_at(i).code);
                    }

                    Console.WriteLine("Instance {0}: x: {1}, y: {2}",
                        data_seq.get_at(i).code, data_seq.get_at(i).x,
                        data_seq.get_at(i).y);
                }
                else
                {
                    /* Since there is not valid data, it may include metadata */
                    keys dummy = new keys();
                    try
                    {
                        DDS.InstanceHandle_t temp =
                            info_seq.get_at(i).instance_handle;
                        keys_reader.get_key_value(dummy, ref temp);
                    }
                    catch (DDS.Exception e)
                    {
                        Console.WriteLine("get_key_value error {0}", e);
                    }

                    /* Here we print a message if the instance state is ALIVE_NO_WRITERS or ALIVE_DISPOSED */
                    if (info_seq.get_at(i).instance_state ==
                        DDS.InstanceStateKind.NOT_ALIVE_NO_WRITERS_INSTANCE_STATE)
                    {
                        Console.WriteLine("Instance {0} has no writers",
                            dummy.code);
                    }
                    else if (info_seq.get_at(i).instance_state ==
                        DDS.InstanceStateKind.NOT_ALIVE_DISPOSED_INSTANCE_STATE)
                    {
                        Console.WriteLine("Instance {0} disposed",
                            dummy.code);
                    }
                }
                /* End changes for Keyed_Data */
            }

            try {
                keys_reader.return_loan(data_seq, info_seq);
            }
            catch(DDS.Exception e) {
                Console.WriteLine("return loan error {0}", e);
            }
        }

        public keysListener() {
            data_seq = new keysSeq();
            info_seq = new DDS.SampleInfoSeq();
        }

        private keysSeq data_seq;
        private DDS.SampleInfoSeq info_seq;
    };

    public static void Main(string[] args) {

        // --- Get domain ID --- //
        int domain_id = 0;
        if (args.Length >= 1) {
            domain_id = Int32.Parse(args[0]);
        }

        // --- Get max loop count; 0 means infinite loop  --- //
        int sample_count = 0;
        if (args.Length >= 2) {
            sample_count = Int32.Parse(args[1]);
        }

        /* Uncomment this to turn on additional logging
        NDDS.ConfigLogger.get_instance().set_verbosity_by_category(
            NDDS.LogCategory.NDDS_CONFIG_LOG_CATEGORY_API, 
            NDDS.LogVerbosity.NDDS_CONFIG_LOG_VERBOSITY_STATUS_ALL);
        */

        // --- Run --- //
        try {
            keysSubscriber.subscribe(
                domain_id, sample_count);
        }
        catch(DDS.Exception) {
            Console.WriteLine("error in subscriber");
        }
    }

    static void subscribe(int domain_id, int sample_count) {

        // --- Create participant --- //

        /* To customize the participant QoS, use 
           the configuration file USER_QOS_PROFILES.xml */
        DDS.DomainParticipant participant =
            DDS.DomainParticipantFactory.get_instance().create_participant(
                domain_id,
                DDS.DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT, 
                null /* listener */,
                DDS.StatusMask.STATUS_MASK_NONE);
        if (participant == null) {
            shutdown(participant);
            throw new ApplicationException("create_participant error");
        }

        // --- Create subscriber --- //

        /* To customize the subscriber QoS, use 
           the configuration file USER_QOS_PROFILES.xml */
        DDS.Subscriber subscriber = participant.create_subscriber(
            DDS.DomainParticipant.SUBSCRIBER_QOS_DEFAULT,
            null /* listener */,
            DDS.StatusMask.STATUS_MASK_NONE);
        if (subscriber == null) {
            shutdown(participant);
            throw new ApplicationException("create_subscriber error");
        }

        // --- Create topic --- //

        /* Register the type before creating the topic */
        System.String type_name = keysTypeSupport.get_type_name();
        try {
            keysTypeSupport.register_type(
                participant, type_name);
        }
        catch(DDS.Exception e) {
            Console.WriteLine("register_type error {0}", e);
            shutdown(participant);
            throw e;
        }

        /* To customize the topic QoS, use 
           the configuration file USER_QOS_PROFILES.xml */
        DDS.Topic topic = participant.create_topic(
            "Example keys",
            type_name,
            DDS.DomainParticipant.TOPIC_QOS_DEFAULT,
            null /* listener */,
            DDS.StatusMask.STATUS_MASK_NONE);
        if (topic == null) {
            shutdown(participant);
            throw new ApplicationException("create_topic error");
        }

        // --- Create reader --- //

        /* Create a data reader listener */
        keysListener reader_listener =
            new keysListener();

        /* To customize the data reader QoS, use 
           the configuration file USER_QOS_PROFILES.xml */
        DDS.DataReader reader = subscriber.create_datareader(
            topic,
            DDS.Subscriber.DATAREADER_QOS_DEFAULT,
            reader_listener,
            DDS.StatusMask.STATUS_MASK_ALL);
        if (reader == null) {
            shutdown(participant);
            reader_listener = null;
            throw new ApplicationException("create_datareader error");
        }

        // --- Wait for data --- //

        /* Main loop */
        const System.Int32 receive_period = 1000; // milliseconds
        for (int count=0;
             (sample_count == 0) || (count < sample_count);
             ++count) {
            //Console.WriteLine("keys subscriber sleeping for {0} sec...",receive_period / 1000);

            System.Threading.Thread.Sleep(receive_period);
        }

        // --- Shutdown --- //

        /* Delete all entities */
        shutdown(participant);
        reader_listener = null;
    }


    static void shutdown(
        DDS.DomainParticipant participant) {

        /* Delete all entities */

        if (participant != null) {
            participant.delete_contained_entities();
            DDS.DomainParticipantFactory.get_instance().delete_participant(
                ref participant);
        }

        /* RTI Connext provides finalize_instance() method on
           domain participant factory for users who want to release memory
           used by the participant factory. Uncomment the following block of
           code for clean destruction of the singleton. */
        /*
        try {
            DDS.DomainParticipantFactory.finalize_instance();
        }
        catch(DDS.Exception e) {
            Console.WriteLine("finalize_instance error {0}", e);
            throw e;
        }
        */
    }
}


