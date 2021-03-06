package RTMExamples.TopicTest;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Observer;
import java.util.Observable;

import jp.go.aist.rtm.RTC.DataFlowComponentBase;
import jp.go.aist.rtm.RTC.Manager;
import jp.go.aist.rtm.RTC.connectorListener.ReturnCode;
import jp.go.aist.rtm.RTC.port.OutPort;
import jp.go.aist.rtm.RTC.port.ConnectorBase;
import jp.go.aist.rtm.RTC.port.ConnectorListener;
import jp.go.aist.rtm.RTC.port.ConnectorDataListener;
import jp.go.aist.rtm.RTC.port.ConnectorDataListenerT;
import jp.go.aist.rtm.RTC.port.ConnectorDataListenerType;
import jp.go.aist.rtm.RTC.port.ConnectorListenerType;
import jp.go.aist.rtm.RTC.util.DataRef;
import RTC.ReturnCode_t;
import RTC.TimedLong;
import RTC.PortService;
import RTC.PortServiceListHolder;

import org.omg.CORBA.portable.OutputStream;

import jp.go.aist.rtm.RTC.port.CorbaPort;
import jp.go.aist.rtm.RTC.port.CorbaConsumer;

import RTMExamples.SimpleService.MyService;



public class ConsoleInImpl extends DataFlowComponentBase {
    private class TopicCorbaPort<DataType> extends CorbaPort {
        public TopicCorbaPort(final String name) {
            super(name);
            appendProperty("publish_topic","test");
        }
    }
    private class TopicOutPort<DataType> extends OutPort {
        public TopicOutPort(final String name, DataRef<DataType> valueRef) {
            super(name,valueRef);
            appendProperty("publish_topic","test");
        }
    }

    public ConsoleInImpl(Manager manager) {
        super(manager);
        // <rtc-template block="initializer">
        m_topic_out_val = new TimedLong(new RTC.Time(0,0),0);
        m_topic_out = new DataRef<TimedLong>(m_topic_out_val);
        m_topic_outOut = new TopicOutPort<TimedLong>("topic_out", m_topic_out);

        m_ServicePort = new TopicCorbaPort("topic_service");
        // </rtc-template>

        // Registration: InPort/OutPort/Service
        // <rtc-template block="registration">
        // Set InPort buffers
        
        // Set OutPort buffer
        // Set service provider to Ports
        
        // Set service consumers to Ports
        
        // Set CORBA Service Ports
        
        // </rtc-template>
    }

    // The initialize action (on CREATED->ALIVE transition)
    // formaer rtc_init_entry() 
    @Override
    protected ReturnCode_t onInitialize() {
        addOutPort("topic_out", m_topic_outOut);
        m_ServicePort.registerConsumer("topic_service", 
                                            "Service", 
                                            m_myservice0Base);
        // Set CORBA Service Ports
        addPort(m_ServicePort);
        return super.onInitialize();
    }
    // The finalize action (on ALIVE->END transition)
    // formaer rtc_exiting_entry()
//    @Override
//    protected ReturnCode_t onFinalize() {
//        return super.onFinalize();
//    }
    //
    // The startup action when ExecutionContext startup
    // former rtc_starting_entry()
//    @Override
//    protected ReturnCode_t onStartup(int ec_id) {
//        return super.onStartup(ec_id);
//    }
    //
    // The shutdown action when ExecutionContext stop
    // former rtc_stopping_entry()
//    @Override
//    protected ReturnCode_t onShutdown(int ec_id) {
//        return super.onShutdown(ec_id);
//    }
    //
    // The activated action (Active state entry action)
    // former rtc_active_entry()
//    @Override
//    protected ReturnCode_t onActivated(int ec_id) {
//        return super.onActivated(ec_id);
//    }
    //
    // The deactivated action (Active state exit action)
    // former rtc_active_exit()
//    @Override
//    protected ReturnCode_t onDeactivated(int ec_id) {
//        return super.onDeactivated(ec_id);
//    }
    //
    // The execution action that is invoked periodically
    // former rtc_active_do()
    @Override
    protected ReturnCode_t onExecute(int ec_id) {
        System.out.print("Please input : ");
        BufferedReader buff = new BufferedReader(new InputStreamReader( System.in ));
        String str = null;
        try {
            str = buff.readLine();
            if(str != null){
                m_topic_out_val.data = Integer.parseInt(str);
            }
            System.out.println("Sending to subscriber: "  
                               + m_topic_out_val.data);
            m_topic_outOut.write();
        } catch (Exception e) {
            if(str != null){
                String[] argv = str.split(" ");
                m_myservice0 = m_myservice0Base._ptr();
                if( argv[0].equals("echo") && argv.length>1 ) {
                    String retmsg = m_myservice0.echo(argv[1]);
                    System.out.println( "echo return: " + retmsg );
                }
                else if( argv[0].equals("set_value") && argv.length>1 ) {
                    Float val = Float.valueOf(argv[1]);
                    m_myservice0.set_value(val.floatValue());
                    System.out.println( "Set remote value: " + val );
                }
          
                else if( argv[0].equals("get_value") ) {
                    System.out.println( "Current remote value: " 
                                        + m_myservice0.get_value() );
                }
          
                else if( argv[0].equals("get_echo_history") ) {
                    String[] echo_history = m_myservice0.get_echo_history();
                    for( int intIdx=0;intIdx<echo_history.length;intIdx++ ) {
                        System.out.println( intIdx+": "+echo_history[intIdx]);
                    }
                }
                else if( argv[0].equals("get_value_history") ) {
                    float[] value_history = m_myservice0.get_value_history();
                    for( int intIdx=0;intIdx<value_history.length;intIdx++ ) {
                        System.out.println( intIdx+": "+value_history[intIdx]);
                    }
                }
                else{
                    System.out.println("");
                    System.out.println("Command list: ");
                    System.out.println(" echo [msg]       : echo message.");
                    System.out.println(" set_value [value]: set value." );
                    System.out.println(" get_value        : "
                                       +"get current value.");
                    System.out.println(" get_echo_history : "
                                       +"get input messsage history." );
                    System.out.println(" get_value_history: "
                                       +"get input value history." );
                }
            }
            else{
                System.out.println("Input Error!");
            }
        }

        return super.onExecute(ec_id);
    }
    //
    // The aborting action when main logic error occurred.
    // former rtc_aborting_entry()
//  @Override
//  public ReturnCode_t onAborting(int ec_id) {
//      return super.onAborting(ec_id);
//  }
    //
    // The error action in ERROR state
    // former rtc_error_do()
//    @Override
//    public ReturnCode_t onError(int ec_id) {
//        return super.onError(ec_id);
//    }
    //
    // The reset action that is invoked resetting
    // This is same but different the former rtc_init_entry()
//    @Override
//    protected ReturnCode_t onReset(int ec_id) {
//        return super.onReset(ec_id);
//    }
//  
    // The state update action that is invoked after onExecute() action
    // no corresponding operation exists in OpenRTm-aist-0.2.0
//    @Override
//    protected ReturnCode_t onStateUpdate(int ec_id) {
//        return super.onStateUpdate(ec_id);
//    }
    //
    // The action that is invoked when execution context's rate is changed
    // no corresponding operation exists in OpenRTm-aist-0.2.0
//    @Override
//    protected ReturnCode_t onRateChanged(int ec_id) {
//        return super.onRateChanged(ec_id);
//    }
//
    // DataInPort declaration
    // <rtc-template block="inport_declare">
    
    // </rtc-template>

    // DataOutPort declaration
    // <rtc-template block="outport_declare">
    protected TimedLong m_topic_out_val;
    protected DataRef<TimedLong> m_topic_out;
    protected TopicOutPort<TimedLong> m_topic_outOut;
    // </rtc-template>

    // CORBA Port declaration
    // <rtc-template block="corbaport_declare">
    protected TopicCorbaPort m_ServicePort;
    
    // </rtc-template>

    // Service declaration
    // <rtc-template block="service_declare">
    protected CorbaConsumer<MyService> m_myservice0Base =
        new CorbaConsumer<MyService>(MyService.class);
    
    protected MyService m_myservice0;
    //protected MyServiceSVC_impl m_service = new MyServiceSVC_impl();
    
    // </rtc-template>

    // Consumer declaration
    // <rtc-template block="consumer_declare">
    
    // </rtc-template>

    class DataListener extends ConnectorDataListenerT<TimedLong>{
        public DataListener(final String name){
            super(TimedLong.class);
            m_name = name;
        }

        public ReturnCode operator(final ConnectorBase.ConnectorInfo arg,
                               final TimedLong data) {
            ConnectorBase.ConnectorInfo info =(ConnectorBase.ConnectorInfo)arg;
            System.out.println("------------------------------");
            System.out.println("Listener:       "+m_name);
            System.out.println("Profile::name:  "+info.name);
            System.out.println("Profile::id:    "+info.id);
            System.out.println("Data:           "+data.data);
            System.out.println("------------------------------");
            return ReturnCode.NO_CHANGE;
        }
        public String m_name;
    }
    class Listener extends ConnectorListener{
        public Listener(final String name){
            m_name = name;
        }

        public ReturnCode operator(final ConnectorBase.ConnectorInfo arg){
            System.out.println("------------------------------");
            System.out.println("Listener:          "+m_name);
            System.out.println("Profile::name:     "+arg.name);
            System.out.println("Profile::id:       "+arg.id);
            String str = new String();
            System.out.println("Profile::data_type:"+arg.properties.getProperty("data_type"));
            System.out.println("------------------------------");
            return ReturnCode.NO_CHANGE;
        }
        public String m_name;
    }
}

