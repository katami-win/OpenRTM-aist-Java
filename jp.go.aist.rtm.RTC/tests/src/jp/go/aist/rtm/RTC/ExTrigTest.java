package jp.go.aist.rtm.RTC;


import jp.go.aist.rtm.RTC.port.CorbaConsumer;
import jp.go.aist.rtm.RTC.util.Properties;
import jp.go.aist.rtm.RTC.port.OutPort;
import jp.go.aist.rtm.RTC.DataFlowComponentBase;
import jp.go.aist.rtm.RTC.util.DataRef;
import jp.go.aist.rtm.RTC.executionContext.ExtTrigExecutionContext;

import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import RTC.ComponentProfile;
import RTC.ExecutionContextListHolder;
import RTC.ExecutionContextService;
import RTC.ExecutionContextServiceListHolder;
import RTC.ExecutionKind;
import OpenRTM.ExtTrigExecutionContextService;
import RTC.LifeCycleState;
import RTC.LightweightRTObject;
import RTC.PortServiceListHolder;
import RTC.RTObject;
import RTC.ReturnCode_t;
import RTC.TimedLong;
import _SDOPackage.NVListHolder;
import OpenRTM.DataFlowComponent;

/**
* ExtTrigger　テスト
* 対象クラス：ExtTrigExecutionContext
*/
public class ExTrigTest extends SampleTest {
    private String configPath;
    private RTObject_impl comp;
    private Manager manager;
    private ExtTrigExecutionContextService ec1Ref;
    private RTObject coninRef;

    public class TestComp extends DataFlowComponentBase {
        @Override
        protected ReturnCode_t onInitialize() {
            try {
                addOutPort("out", m_outOut);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return super.onInitialize();
        }
        public TestComp(Manager manager) {
            super(manager);
            m_out_val = new TimedLong(new RTC.Time(0,0),0);
            m_out = new DataRef<TimedLong>(m_out_val);
            m_outOut = new OutPort<TimedLong>("out", m_out);
        }
        protected TimedLong m_out_val;
        protected DataRef<TimedLong> m_out;
        protected OutPort<TimedLong> m_outOut;
    }
    public class TestCompDelete implements RtcDeleteFunc {

        public void deleteRtc(RTObject_impl rtcBase) {
          rtcBase = null;
        }

    }
    public class TestCompNew implements RtcNewFunc {

        public DataFlowComponentBase createRtc(Manager mgr) {
            return new TestComp(mgr);
       }
    }

    protected void setUp() throws Exception {
        super.setUp();
        String args[] = {
            "-o","corba.nameservers:localhost",
            "-o","naming.formats:%n.rtc",
            "-o","corba.id:omniORB",
            "-o","corba.endpoint:",
            "-o","corba.args:-ORBInitialHost localhost -ORBInitialPort 2809",
            "-o","naming.enable:Yes",
            "-o","logger.file_name:logging",
            "-o","timer.enable:yes",
            "-o","timer.tick:1000",
            "-o","logger.enable:no",
            "-o","manager.name:test",
            "-o","exec_cxt.periodic.rate:1",
            "-o","exec_cxt.periodic.type:jp.go.aist.rtm.RTC.executionContext.ExtTrigExecutionContext",
        };
        manager = Manager.init(args);
        manager.activateManager();
        String component_conf[] = {
                "implementation_id", "ConsoleIn",
                "type_name",         "ConsoleIn",
                "description",       "Console input component",
                "version",           "1.0",
                "vendor",            "Noriaki Ando, AIST",
                "category",          "example",
                "activity_type",     "DataFlowComponent",
                "max_instance",      "10",
                "language",          "C++",
                "lang_type",         "compile",
                ""
                };
        //registers the module
        Properties prop = new Properties(component_conf);
        manager.registerFactory(prop, new TestCompNew(), new TestCompDelete());
        comp = manager.createComponent("ConsoleIn");
        //
        CorbaConsumer<DataFlowComponent> conin 
            = new CorbaConsumer<DataFlowComponent>(DataFlowComponent.class);
 
        CorbaConsumer<ExtTrigExecutionContextService> ec1 = new CorbaConsumer<ExtTrigExecutionContextService>(ExtTrigExecutionContextService.class);
        CorbaNaming naming = null;
        try {
          naming = new CorbaNaming(manager.getORB(), "localhost:2809");
        } catch (Exception e) {
          e.printStackTrace();
        }
        // find ConsoleIn0 component
        try {
          ComponentProfile prof = comp.get_component_profile();
          conin.setObject(naming.resolve(prof.instance_name+".rtc"));
        } catch (NotFound e) {
          e.printStackTrace();
        } catch (CannotProceed e) {
          e.printStackTrace();
        } catch (InvalidName e) {
          e.printStackTrace();
        }

        ExecutionContextListHolder eclisti = new ExecutionContextListHolder();
        eclisti.value = new ExecutionContextService[0];
        coninRef = conin._ptr();
        eclisti.value =  coninRef.get_owned_contexts();
        eclisti.value[0].activate_component(coninRef);
        ec1.setObject(eclisti.value[0]);
        ec1Ref = ec1._ptr();
    }
    protected void tearDown() throws Exception {
        ExecutionContextListHolder execlist = new ExecutionContextListHolder();
        execlist.value = coninRef.get_owned_contexts();
        Thread.yield();
        execlist.value[0].stop();
        super.tearDown();
        manager.shutdownComponents();
        manager.shutdownNaming();
        manager = null;
    }

    /**
     *<pre>
     * ComponentProfileのチェック
     *　・設定したComponentProfileが取得できるか？
     *　・設定したPort情報を取得できるか？
     *　・設定したPortProfileを取得できるか？
     *</pre>
     */
    public void test_profile() {
        ComponentProfile prof = comp.get_component_profile();
        assertEquals("ConsoleIn", comp.get_component_profile().type_name);
        assertEquals("Console input component", comp.get_component_profile().description);
        assertEquals("1.0", comp.get_component_profile().version);
        assertEquals("Noriaki Ando, AIST", comp.get_component_profile().vendor);
        assertEquals("example", comp.get_component_profile().category);
        //
        PortServiceListHolder portlist = new PortServiceListHolder(comp.get_ports());
        assertEquals( 1, portlist.value.length);
        assertEquals( comp.get_component_profile().instance_name+".out", portlist.value[0].get_port_profile().name);
        //
        Properties prop = new Properties();
        this.copyToProperties(prop, new NVListHolder(portlist.value[0].get_port_profile().properties));
        assertEquals( "DataOutPort", prop.getProperty("port.port_type"));
        assertEquals( "IDL:RTC/TimedLong:1.0", prop.getProperty("dataport.data_type"));
        assertEquals( "shared_memory,data_service,direct,corba_cdr", prop.getProperty("dataport.interface_type"));
        assertEquals( "push,pull", prop.getProperty("dataport.dataflow_type"));
        assertEquals( "new, nonblock, flush, periodic, block", prop.getProperty("dataport.subscription_type"));
        //
    }
    
    /**
     *<pre>
     * ExecutionContextのチェック
     *  ・RTCのalive状態を取得できるか？
     *　・ExecutionContextの実行状態を取得できるか？
     *　・ExecutionContextの種類を取得できるか？
     *　・ExecutionContextの更新周期を設定できるか？
     *　・ExecutionContextの更新周期を取得できるか？
     *　・ExecutionContextを停止できるか？
     *　・停止したExecutionContextを再度停止した場合にエラーが返ってくるか？
     *　・ExecutionContextを開始できるか？
     *　・開始したExecutionContextを再度開始した場合にエラーが返ってくるか？
     *</pre>
     */
    public void test_EC() {
        ExecutionContextListHolder execlist = new ExecutionContextListHolder();
        execlist.value = comp.get_owned_contexts();
        assertEquals(true, comp.is_alive(execlist.value[0]));
        assertEquals(true, execlist.value[0].is_running());
        assertEquals(ExecutionKind.PERIODIC, execlist.value[0].get_kind());
        assertEquals(1.0, execlist.value[0].get_rate());
        //
        ReturnCode_t result = execlist.value[0].stop();
        Thread.yield();
        assertEquals(ReturnCode_t.RTC_OK, result);
        assertEquals(false, execlist.value[0].is_running());
        result = execlist.value[0].stop();
        Thread.yield();
        assertEquals(ReturnCode_t.PRECONDITION_NOT_MET, result);
        //
        result = execlist.value[0].start();
        Thread.yield();
        assertEquals(ReturnCode_t.RTC_OK, result);
        assertEquals(true, execlist.value[0].is_running());
        result = execlist.value[0].start();
        Thread.yield();
        assertEquals(ReturnCode_t.PRECONDITION_NOT_MET, result);
        //
    }


    /**
     *<pre>
     * RTCのチェック
     *  ・RTCの状態を取得できるか？
     *　・Inactive状態でdeactivateした場合にエラーが返ってくるか？
     *　・RTCをactivateできるか？
     *　・Active状態でactivateした場合にエラーが返ってくるか？
     *　・Active状態でresetした場合にエラーが返ってくるか？
     *　・Active状態でfinalizeした場合にエラーが返ってくるか？
     *　・RTCをfinalizeできるか？
     *　・RTCをfinalizeしてもalive状態か？
     *　・RTCをexitしたらalive状態から抜けるか？
     *</pre>
     */
    public void test_State() {
        ExecutionContextListHolder execlist = new ExecutionContextListHolder();
        execlist.value = coninRef.get_owned_contexts();
        assertEquals(LifeCycleState.INACTIVE_STATE, execlist.value[0].get_component_state(coninRef));
        ReturnCode_t result = execlist.value[0].deactivate_component(coninRef);
        assertEquals(ReturnCode_t.PRECONDITION_NOT_MET, result);
        result = execlist.value[0].start();
        Thread.yield();
        //
        result = execlist.value[0].activate_component(coninRef);
        Thread.yield();
        assertEquals(ReturnCode_t.RTC_OK, result);
        assertEquals(LifeCycleState.INACTIVE_STATE, execlist.value[0].get_component_state(coninRef));
        ec1Ref.tick();
        Thread.yield();
        assertEquals(LifeCycleState.ACTIVE_STATE, execlist.value[0].get_component_state(coninRef));
        for(int intIdx=0;intIdx<40;intIdx++) {
            ec1Ref.tick();
            Thread.yield();
        }
        result = execlist.value[0].activate_component(coninRef);
        Thread.yield();
        assertEquals(ReturnCode_t.PRECONDITION_NOT_MET, result);
        //
        result = execlist.value[0].reset_component(coninRef);
        Thread.yield();
        assertEquals(ReturnCode_t.PRECONDITION_NOT_MET, result);
        //
        result = comp._finalize();
        Thread.yield();
        assertEquals(ReturnCode_t.PRECONDITION_NOT_MET,  result);
        //
        result = execlist.value[0].stop();
        Thread.yield();
        result = comp._finalize();
        Thread.yield();
        assertEquals(true, comp.is_alive(execlist.value[0]));
        result = comp.exit();
        Thread.yield();
        assertEquals(true, comp.is_alive(execlist.value[0]));
    }

}
