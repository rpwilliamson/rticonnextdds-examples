
/* profilesPublisher.java

   A publication of data of type profiles

   This file is derived from code automatically generated by the rtiddsgen 
   command:

   rtiddsgen -language java -example <arch> .idl

   Example publication of type profiles automatically generated by 
   'rtiddsgen' To test them follow these steps:

   (1) Compile this file and the example subscription.

   (2) Start the subscription with the command
       java profilesSubscriber <domain_id> <sample_count>
       
   (3) Start the publication with the command
       java profilesPublisher <domain_id> <sample_count>

   (4) [Optional] Specify the list of discovery initial peers and 
       multicast receive addresses via an environment variable or a file 
       (in the current working directory) called NDDS_DISCOVERY_PEERS.  
       
   You can run any number of publishers and subscribers programs, and can 
   add and remove them dynamically from the domain.
              
   Example:
        
       To run the example application on domain <domain_id>:
            
       Ensure that $(NDDSHOME)/lib/<arch> is on the dynamic library path for
       Java.                       
       
        On Unix: 
             add $(NDDSHOME)/lib/<arch> to the 'LD_LIBRARY_PATH' environment
             variable
                                         
        On Windows:
             add %NDDSHOME%\lib\<arch> to the 'Path' environment variable
                        

       Run the Java applications:
       
        java -Djava.ext.dirs=$NDDSHOME/class profilesPublisher <domain_id>

        java -Djava.ext.dirs=$NDDSHOME/class profilesSubscriber <domain_id>        

       
       
modification history
------------ -------         
*/

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import com.rti.dds.domain.*;
import com.rti.dds.infrastructure.*;
import com.rti.dds.publication.*;
import com.rti.dds.topic.*;
import com.rti.ndds.config.*;

// ===========================================================================

public class profilesPublisher {
    // -----------------------------------------------------------------------
    // Public Methods
    // -----------------------------------------------------------------------
    
    public static void main(String[] args) {
        // --- Get domain ID --- //
        int domainId = 0;
        if (args.length >= 1) {
            domainId = Integer.valueOf(args[0]).intValue();
        }

        // -- Get max loop count; 0 means infinite loop --- //
        int sampleCount = 0;
        if (args.length >= 2) {
            sampleCount = Integer.valueOf(args[1]).intValue();
        }
        
        /* Uncomment this to turn on additional logging
        Logger.get_instance().set_verbosity_by_category(
            LogCategory.NDDS_CONFIG_LOG_CATEGORY_API,
            LogVerbosity.NDDS_CONFIG_LOG_VERBOSITY_STATUS_ALL);
        */
        
        // --- Run --- //
        publisherMain(domainId, sampleCount);
    }
    
    
    
    // -----------------------------------------------------------------------
    // Private Methods
    // -----------------------------------------------------------------------
    
    // --- Constructors: -----------------------------------------------------
    
    private profilesPublisher() {
        super();
    }
    
    
    // -----------------------------------------------------------------------
    
    private static void publisherMain(int domainId, int sampleCount) {

        DomainParticipant participant = null;
        Publisher publisher = null;
        Topic topic = null;
        profilesDataWriter volatileWriter = null;
        profilesDataWriter transientLocalWriter = null;

        try {
            /* There are several different approaches for loading QoS profiles
             * from XML files (see Configuring QoS with XML chapter in the RTI  
             * Connext Core Libraries and Utilities User's Manual). In this  
             * example we illustrate two of them:
             *
             * 1) Creating a file named USER_QOS_PROFILES.xml, which is loaded,
             * automatically by the DomainParticipantFactory. In this case, the 
             * file defines a QoS profile named volatile_profile that  
             * configures reliable, volatile DataWriters and DataReaders.
             *
             * 2) Adding XML documents to the DomainParticipantFactory using 
             * its Profile QoSPolicy (DDS Extension). In this case, we add
             * my_custom_qos_profiles.xml to the url_profile sequence, which 
             * stores the URLs of all the XML documents with QoS policies that
             * are loaded by the DomainParticipantFactory aside from the ones 
             * that are automatically loaded.
             * my_custom_qos_profiles.xml defines a QoS profile named
             * transient_local_profile that configures reliable, transient 
             * local DataWriters and DataReaders.
             */
        	
        	// --- Create participant --- //
    
            /* To load my_custom_qos_profiles.xml, as explained above, we need  
             * to modify the DDSTheParticipantFactory Profile QoSPolicy */
        	DomainParticipantFactoryQos factoryQos = 
        		new DomainParticipantFactoryQos();
        	DomainParticipantFactory.TheParticipantFactory.get_qos(factoryQos);

        	/* We are only going to add one XML file to the url_profile 
        	 * sequence, so we set a maximum length of 1. */
        	factoryQos.profile.url_profile.setMaximum(1);

        	/* The XML file will be loaded from the working directory. That 
        	 * means, you need to run the example like this:
             * ./objs/<architecture>/profiles_publisher
             * (see README.txt for more information on how to run the example).
             *
             * Note that you can specify the absolute path of the XML QoS file 
             * to avoid this problem.
             */
        	factoryQos.profile
        					.url_profile
        						.add("file://my_custom_qos_profiles.xml");
        	DomainParticipantFactory.TheParticipantFactory.set_qos(factoryQos);
    
            /* Our default Qos profile, volatile_profile, sets the participant 
             * name. This is the only participant_qos policy that we change in 
             * our example. As this is done in the default QoS profile, we 
             * don't need to specify its name, so we can create the participant
             * using the create_participant() method rather than using
             * create_participant_with_profile().  */    
            participant = DomainParticipantFactory.TheParticipantFactory.
                create_participant(
                    domainId, DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT,
                    null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (participant == null) {
                System.err.println("create_participant error\n");
                return;
            }        
                    
            // --- Create publisher --- //
    
            /* We haven't changed the publisher_qos in any of QoS profiles we 
             * use in this example, so we can just use the create_publisher() 
             * method. If you want to load an specific profile in which you may
             * have changed the publisher_qos, use the 
             * create_publisher_with_profile() method. */
    
            publisher = participant.create_publisher(
                DomainParticipant.PUBLISHER_QOS_DEFAULT, null /* listener */,
                StatusKind.STATUS_MASK_NONE);
            if (publisher == null) {
                System.err.println("create_publisher error\n");
                return;
            }                   
                
        
            // --- Create topic --- //

            /* Register type before creating topic */
            String typeName = profilesTypeSupport.get_type_name();
            profilesTypeSupport.register_type(participant, typeName);
    
            /* We haven't changed the topic_qos in any of QoS profiles we use
             * in this example, so we can just use the create_topic() method.
             * If you want to load an specific profile in which you may have
             * changed the topic_qos, use the create_topic_with_profile() 
             * method. */
    
            topic = participant.create_topic(
                "Example profiles",
                typeName, DomainParticipant.TOPIC_QOS_DEFAULT,
                null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (topic == null) {
                System.err.println("create_topic error\n");
                return;
            }           
                
            // --- Create writer --- //
    
            /* Volatile writer -- As volatile_profile is the default qos 
             * profile we don't need to specify the profile we are going to 
             * use, we can just call create_datawriter passing 
             * DDS_DATAWRITER_QOS_DEFAULT. */
    
            volatileWriter = (profilesDataWriter)
                publisher.create_datawriter(
                    topic, Publisher.DATAWRITER_QOS_DEFAULT,
                    null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (volatileWriter == null) {
                System.err.println("create_datawriter error\n");
                return;
            }           
                                        
            /* Transient Local writer -- In this case we use
             * create_datawriter_with_profile, because we have to use a profile
             * other than the default one. This profile has been defined in
             * my_custom_qos_profiles.xml, but since we already loaded the XML
             * file we don't need to specify anything else. */
            transientLocalWriter = (profilesDataWriter)
            	publisher.create_datawriter_with_profile(
            			topic, "profiles_Library", 
            			"transient_local_profile",
            			null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (transientLocalWriter == null) {
                System.err.println("create_datawriter error\n");
                return;
            }           
            
            // --- Write --- //

            /* Create data sample for writing */
            profiles instance = new profiles();

            InstanceHandle_t instance_handle = InstanceHandle_t.HANDLE_NIL;
            /* For a data type that has a key, if the same instance is going to be
               written multiple times, initialize the key here
               and register the keyed instance prior to writing */
            //instance_handle = writer.register_instance(instance);

            final long sendPeriodMillis = 4 * 1000; // 4 seconds

            for (int count = 0;
                 (sampleCount == 0) || (count < sampleCount);
                 ++count) {
                System.out.println("Writing profiles, count " + count);

                /* Modify the instance to be written here */
                instance.profile_name = "volatile_profile";
                instance.x = count;

                System.out.println("Writing profile_name = "
                		+ instance.profile_name + " x = "
                		+ instance.x);

                /* Write data */
                volatileWriter.write(instance, instance_handle);

                instance.profile_name = "transient_local_profile";

                System.out.println("Writing profile_name = "
                		+ instance.profile_name + " x = "
                		+ instance.x);

                /* Write data */
                transientLocalWriter.write(instance, instance_handle);
                            
                try {
                    Thread.sleep(sendPeriodMillis);
                } catch (InterruptedException ix) {
                    System.err.println("INTERRUPTED");
                    break;
                }
            }

            //writer.unregister_instance(instance, instance_handle);

        } finally {

            // --- Shutdown --- //

            if(participant != null) {
                participant.delete_contained_entities();

                DomainParticipantFactory.TheParticipantFactory.
                    delete_participant(participant);
            }
            /* RTI Connext provides finalize_instance()
               method for people who want to release memory used by the
               participant factory singleton. Uncomment the following block of
               code for clean destruction of the participant factory
               singleton. */
            //DomainParticipantFactory.finalize_instance();
        }
    }
}

        