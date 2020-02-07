package jp.go.aist.rtm.RTC.util;

import junit.framework.TestCase;

import java.util.Vector;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.Vector;
import java.io.IOException;
import java.io.Serializable;
import java.io.BufferedReader;
import java.io.InputStreamReader;


import java.lang.Thread;



import jp.go.aist.rtm.RTC.DataFlowComponentBase;
import jp.go.aist.rtm.RTC.Manager;
import jp.go.aist.rtm.RTC.ModuleInitProc;
import jp.go.aist.rtm.RTC.RTObject_impl;
import jp.go.aist.rtm.RTC.CorbaNaming;
import jp.go.aist.rtm.RTC.RTObject_impl;
import jp.go.aist.rtm.RTC.executionContext.ExecutionContextWorker;
import jp.go.aist.rtm.RTC.port.CorbaConsumer;
import jp.go.aist.rtm.RTC.port.CorbaPort;
import jp.go.aist.rtm.RTC.port.InPort;
import jp.go.aist.rtm.RTC.port.OutPort;
import jp.go.aist.rtm.RTC.util.CORBA_SeqUtil;
import jp.go.aist.rtm.RTC.util.NVUtil;
import jp.go.aist.rtm.RTC.util.ORBUtil;
import jp.go.aist.rtm.RTC.util.StringUtil;
import jp.go.aist.rtm.RTC.util.Properties;
import jp.go.aist.rtm.RTC.util.CORBA_RTCUtil;
import jp.go.aist.rtm.RTC.RtcDeleteFunc;
import jp.go.aist.rtm.RTC.RtcNewFunc;
import jp.go.aist.rtm.RTC.RegisterModuleFunc;
import jp.go.aist.rtm.RTC.NamingManager;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import RTC.ComponentProfile;
import RTC.ConnectorProfile;
import RTC.ConnectorProfileHolder;
import RTC.ExecutionContext;
import RTC.ExecutionContextListHolder;
import RTC.ExecutionContextService;
import RTC.ExecutionContextServiceHelper;
import RTC.ExecutionContextProfile;
import RTC.LifeCycleState;
import RTC.PortService;
import RTC.PortServiceListHolder;
import RTC.RTObject;
import RTC.RTObjectHolder;
import RTC.RTObjectHelper;
import RTC.ReturnCode_t;
import RTC.TimedLong;
import OpenRTM.DataFlowComponent;
import OpenRTM.DataFlowComponentHelper;
import _SDOPackage.NVListHolder;
import _SDOPackage.NameValue;


public class CORBA_RTCUtilTest extends TestCase {
    private Manager m_manager = null;
    private ORB m_orb;
    private CorbaConsumer<DataFlowComponent> m_conout =
            new CorbaConsumer<DataFlowComponent>(DataFlowComponent.class);
    private CorbaConsumer<DataFlowComponent> m_conin =
            new CorbaConsumer<DataFlowComponent>(DataFlowComponent.class);
    private CorbaConsumer<DataFlowComponent> m_conin2 =
            new CorbaConsumer<DataFlowComponent>(DataFlowComponent.class);
    private CorbaConsumer<DataFlowComponent> m_sercon =
            new CorbaConsumer<DataFlowComponent>(DataFlowComponent.class);
    private CorbaConsumer<DataFlowComponent> m_config =
            new CorbaConsumer<DataFlowComponent>(DataFlowComponent.class);

    private RTObjectHolder m_conoutRef
                    = new RTObjectHolder();
    private RTObjectHolder m_coninRef
                    = new RTObjectHolder();
    private RTObjectHolder m_coninRef2
                    = new RTObjectHolder();
    private RTObjectHolder m_serconRef
                    = new RTObjectHolder();
    private RTObjectHolder m_configRef
                    = new RTObjectHolder();

    //private RTObject m_conoutRef;
    private ExecutionContextListHolder m_eclisto 
                    = new ExecutionContextListHolder();
    private ExecutionContextListHolder m_eclisti 
                    = new ExecutionContextListHolder();
    private ExecutionContextListHolder m_eclisti2 
                    = new ExecutionContextListHolder();
    private ExecutionContextListHolder m_eclistseq 
                    = new ExecutionContextListHolder();

    private RTObject_impl m_out_impl;
    private RTObject_impl m_in_impl;
    private RTObject_impl m_in2_impl;
    private RTObject_impl m_out_seq_impl;
    private RTObject_impl m_config_impl;
 

    String m_conout_name;
    String m_conin_name;
    String m_conin2_name;
    String m_conser_name;
    String m_conconf_name;

    protected void setUp() throws Exception {
        super.setUp();
        m_eclisti = new ExecutionContextListHolder();
        m_eclisti2 = new ExecutionContextListHolder();
        m_eclistseq = new ExecutionContextListHolder();

        m_conoutRef = new RTObjectHolder();
        m_coninRef = new RTObjectHolder();
        m_coninRef2 = new RTObjectHolder();
        m_serconRef = new RTObjectHolder();
        m_configRef = new RTObjectHolder();

        String[] args = new String[0];
        String param[] = {
            "-o","logger.enable:no",
            "-o","manager.shutdown_auto:no",
            "-o","naming.formats:%n.rtc",
        };

        m_manager = Manager.init(param);
        // 
        // 
        // 
        m_manager.activateManager();
        // 
        // 
        //
        Properties prop_out = new Properties(console_out_conf);
        m_manager.registerFactory(prop_out, 
                new ConsoleOut(), new ConsoleOut());
        m_out_impl = m_manager.createComponent("ConsoleOut");
        if(m_out_impl==null)
        {
            System.out.println("ConsoleOut is null.");
        }
        //
        Properties prop_in = new Properties(console_in_conf);
        m_manager.registerFactory(prop_in, new ConsoleIn(), new ConsoleIn());
        m_in_impl = m_manager.createComponent("ConsoleIn");
        if(m_in_impl==null)
        {
            System.out.println("ConsoleIn is null.");
        }
        m_in2_impl = m_manager.createComponent("ConsoleIn");
        if(m_in_impl==null)
        {
            System.out.println("ConsoleIn is null.");
        }
        //
        Properties prop_out_seq 
            = new Properties(consumer_conf);
        m_manager.registerFactory(prop_out_seq, 
                new MyServiceConsumer(), new MyServiceConsumer());
        m_out_seq_impl 
            = m_manager.createComponent("MyServiceConsumer");
        if(m_out_seq_impl==null)
        {
            System.out.println("MyServiceConsumer is null.");
        }
        //
        Properties prop_config 
            = new Properties(conf_sample_conf);
        m_manager.registerFactory(prop_config,
                new ConfigSample(), new ConfigSample());
        m_config_impl
            = m_manager.createComponent("ConfigSample");
        if(m_config_impl==null)
        {
            System.out.println("ConfigSample is null.");
        }
        //
        //
        //
        //m_manager.runManager(true);
        // 
        // 
        // 
        //ExecutionContextListHolder eclisto = new ExecutionContextListHolder();
        m_eclisto.value = new ExecutionContext[0];
        m_eclisto.value =  m_out_impl.get_owned_contexts();
        //
        //ExecutionContextListHolder eclisti = new ExecutionContextListHolder();
        m_eclisti.value = new ExecutionContext[0];
        m_eclisti.value =  m_in_impl.get_owned_contexts();
        m_eclisti2.value = new ExecutionContext[0];
        m_eclisti2.value =  m_in2_impl.get_owned_contexts();
        // 
        //ExecutionContextListHolder eclistseq = new ExecutionContextListHolder();
        m_eclistseq.value = new ExecutionContext[0];
        m_eclistseq.value =  m_out_seq_impl.get_owned_contexts();
        //
        // bind
        //
        //System.out.println( "bind0 : "+ m_eclisto.value[0]);
        //System.out.println( "bind1 : "+ m_eclisti.value[0]);
        m_out_impl.bindContext(m_eclisti.value[0]);
        m_eclisto.value =  m_out_impl.get_owned_contexts();
        //System.out.println( "m_eclisto.value.length : "
        //        + m_eclisto.value.length);

        //System.out.println( "bind2 : "+ m_eclisto.value[0]);
        //System.out.println( "bind3 : "+ m_eclisto.value[1]);
        //
        //
        //
        m_orb = ORBUtil.getOrb();
        CorbaNaming naming = null;
        try {
            naming = new CorbaNaming(m_orb, "localhost:2809");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        ComponentProfile prof = m_out_impl.get_component_profile();
        m_conout_name = prof.instance_name;
        //CorbaConsumer<DataFlowComponent> conout =
        //    new CorbaConsumer<DataFlowComponent>(DataFlowComponent.class);
        // find ConsoleOut0 component
        try {
            m_conout.setObject(naming.resolve(prof.instance_name+".rtc"));
        } catch (NotFound e) {
            e.printStackTrace();
        } catch (CannotProceed e) {
            e.printStackTrace();
        } catch (InvalidName e) {
            e.printStackTrace();
        }

        // 
        ExecutionContextListHolder eclist = new ExecutionContextListHolder();
        eclist.value = new ExecutionContext[0];
        m_conoutRef.value = m_conout._ptr();
        eclist.value =  m_conoutRef.value.get_owned_contexts();


        prof = m_in_impl.get_component_profile();
        m_conin_name = prof.instance_name;
        try {
            m_conin.setObject(naming.resolve(prof.instance_name+".rtc"));
        } catch (NotFound e) {
            e.printStackTrace();
        } catch (CannotProceed e) {
            e.printStackTrace();
        } catch (InvalidName e) {
            e.printStackTrace();
        }
        // 
        // 
        m_coninRef.value = m_conin._ptr();


        prof = m_in2_impl.get_component_profile();
        m_conin2_name = prof.instance_name;
        try {
            m_conin2.setObject(naming.resolve(prof.instance_name+".rtc"));
        } catch (NotFound e) {
            e.printStackTrace();
        } catch (CannotProceed e) {
            e.printStackTrace();
        } catch (InvalidName e) {
            e.printStackTrace();
        }
        // 
        // 
        m_coninRef2.value = m_conin2._ptr();
        prof = m_out_seq_impl.get_component_profile();
        m_conser_name = prof.instance_name;
        try {
            m_sercon.setObject(naming.resolve(prof.instance_name+".rtc"));
        } catch (NotFound e) {
            e.printStackTrace();
        } catch (CannotProceed e) {
            e.printStackTrace();
        } catch (InvalidName e) {
            e.printStackTrace();
        }

        // 
        m_serconRef.value = m_sercon._ptr();
        // 
        //
        prof = m_config_impl.get_component_profile();
        m_conconf_name = prof.instance_name;
        try {
            m_config.setObject(naming.resolve(prof.instance_name+".rtc"));
        } catch (NotFound e) {
            e.printStackTrace();
        } catch (CannotProceed e) {
            e.printStackTrace();
        } catch (InvalidName e) {
            e.printStackTrace();
        }

        // 
        m_configRef.value = m_config._ptr();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
	m_out_impl.exit();
	m_in_impl.exit();
        m_in2_impl.exit();
        m_out_seq_impl.exit();
        m_config_impl.exit();
    }

    /**
     *
     * 
     */
    public void test_get_actual_ec() {

        ExecutionContext[] list = m_conoutRef.value.get_owned_contexts();
        ExecutionContext ec = CORBA_RTCUtil.get_actual_ec(m_conoutRef.value,0);
        assertTrue("test:id is 0",ec._is_equivalent(list[0]));

        ec = CORBA_RTCUtil.get_actual_ec(m_conoutRef.value,1);
        assertTrue("test:id is 1",ec._is_equivalent(list[1]));

        ec = CORBA_RTCUtil.get_actual_ec(m_conoutRef.value,2);
        assertTrue("test:id is out of range",ec == null);

        ec = CORBA_RTCUtil.get_actual_ec(null,0);
        assertTrue("test:rtc is null",ec == null);
    }
    /**
     *
     * 
     */
    public void test_get_ec_id(){
        System.out.println( "test_get_ec_id()" );
        ExecutionContext[] list = m_conoutRef.value.get_owned_contexts();
        int id = CORBA_RTCUtil.get_ec_id(m_conoutRef.value, list[0]);
        assertTrue("test:id is 0.",id == 0);

        id = CORBA_RTCUtil.get_ec_id(m_conoutRef.value, list[1]);
        assertTrue("test:id is 1",id == 1);

        id = CORBA_RTCUtil.get_ec_id(m_conoutRef.value, null);
        assertTrue("test:list is null",id == -1);

        id = CORBA_RTCUtil.get_ec_id(null, list[0]);
        assertTrue("test:rtc is null",id == -1);

        id = CORBA_RTCUtil.get_ec_id(m_conoutRef.value, m_eclistseq.value[0]);
        assertTrue("test:not foud",id == -1);
    }
    /**
     *
     * 
     */
    public void test_activate_deactivate(){
        System.out.println( "test_activate_deactivate()" );
        ReturnCode_t ret = CORBA_RTCUtil.activate(null, 0);
        assertTrue(ret == ReturnCode_t.BAD_PARAMETER);

        ret = CORBA_RTCUtil.activate(m_conoutRef.value, 3);
        assertTrue(ret == ReturnCode_t.BAD_PARAMETER);

        ret = CORBA_RTCUtil.deactivate(null, 0);
        assertTrue(ret == ReturnCode_t.BAD_PARAMETER);

        ret = CORBA_RTCUtil.deactivate(m_conoutRef.value, 3);
        assertTrue(ret == ReturnCode_t.BAD_PARAMETER);

        try{
            Thread.sleep(500); 
        }
        catch(InterruptedException e){
        }
        ret = CORBA_RTCUtil.activate(m_conoutRef.value, 0);
        assertTrue(ret == ReturnCode_t.RTC_OK);

        try{
            Thread.sleep(500); 
        }
        catch(InterruptedException e){
        }

        ret = CORBA_RTCUtil.deactivate(m_conoutRef.value, 0);
        assertTrue(ret == ReturnCode_t.RTC_OK);


    }
    /**
     *
     * get_state
     *
     */
    public static String getStateString(LifeCycleState state) {
      final String st[] = {
        "CREATED_STATE",
        "INACTIVE_STATE",
        "ACTIVE_STATE",
        "ERROR_STATE"
      };
        return st[state.value()]; 
    }
    public void test_get_state(){
        LifeCycleState ret = CORBA_RTCUtil.get_state(m_conoutRef.value, 0);
        String str_ret = getStateString(ret);
        System.out.println(str_ret);
        assertTrue("test:inactive_state",str_ret.equals("INACTIVE_STATE"));
        //
        CORBA_RTCUtil.activate(m_conoutRef.value, 0);
        try{
            Thread.sleep(500); 
        }
        catch(InterruptedException e){
        }
        ret = CORBA_RTCUtil.get_state(m_conoutRef.value, 0);
        str_ret = getStateString(ret);
        System.out.println(str_ret);
        assertTrue("test:active_state",str_ret.equals("ACTIVE_STATE"));
        //
        CORBA_RTCUtil.deactivate(m_conoutRef.value, 0);
        try{
            Thread.sleep(500); 
        }
        catch(InterruptedException e){
        }
        ret = CORBA_RTCUtil.get_state(m_conoutRef.value, 0);
        str_ret = getStateString(ret);
        System.out.println(str_ret);
        assertTrue("test:inactive_state",str_ret.equals("INACTIVE_STATE"));
        //
        ret = CORBA_RTCUtil.get_state(null, 0);
        str_ret = getStateString(ret);
        System.out.println(str_ret);
        assertTrue("test:error_state",str_ret.equals("ERROR_STATE"));
        //
        ret = CORBA_RTCUtil.get_state(m_conoutRef.value, 3);
        str_ret = getStateString(ret);
        System.out.println(str_ret);
        assertTrue("test:error_state",str_ret.equals("ERROR_STATE"));
    }
    /**
     *
     * is_in_active
     *
     */
    public void test_is_in_active(){
        boolean ret;
        ret = CORBA_RTCUtil.is_in_active(m_conoutRef.value, 0);
        assertTrue("test:is",!ret);
        //
        CORBA_RTCUtil.activate(m_conoutRef.value, 0);
        try{
            Thread.sleep(500); 
        }
        catch(InterruptedException e){
        }
        ret = CORBA_RTCUtil.is_in_active(m_conoutRef.value, 0);
        assertTrue("test:is",ret);
        //
        CORBA_RTCUtil.deactivate(m_conoutRef.value, 0);
        try{
            Thread.sleep(500); 
        }
        catch(InterruptedException e){
        }
        ret = CORBA_RTCUtil.is_in_active(m_conoutRef.value, 0);
        assertTrue("test:is",!ret);
        //
        ret = CORBA_RTCUtil.is_in_active(null, 0);
        assertTrue("test:is",!ret);
        ret = CORBA_RTCUtil.is_in_active(m_conoutRef.value, 3);
        assertTrue("test:is",!ret);
    }

    /**
     *
     * get_default_rate/set_default_rate
     *
     */
    public void test_get_default_rate_set_default_rate(){

        double ret = CORBA_RTCUtil.get_default_rate(m_conoutRef.value);
        assertTrue("test:get_default_rate 1000.0 get value="+ret,ret == 1000.0);
        CORBA_RTCUtil.set_default_rate(m_conoutRef.value, 500.0);
        ret = CORBA_RTCUtil.get_default_rate(m_conoutRef.value);
        assertTrue("test:get_default_rate 500.0",ret == 500.0);
        //
        //
        //
        ret = CORBA_RTCUtil.get_default_rate(null);
        ReturnCode_t code = CORBA_RTCUtil.set_default_rate(null, 500.0);
        assertTrue("test:set_default_rate",code == ReturnCode_t.BAD_PARAMETER);
        // 
    }
    /**
     *
     * get_current_rate/set_current_rate
     *
     */
    public void test_get_current_rate_set_current_rate(){
        CORBA_RTCUtil.set_default_rate(m_conoutRef.value, 1000.0);
        double ret = CORBA_RTCUtil.get_current_rate(m_conoutRef.value,0);
        assertTrue("test:get_rate 1000.0 get value="+ret,ret == 1000.0);
        ret = CORBA_RTCUtil.get_current_rate(m_conoutRef.value,1);
        assertTrue("test:get_rate 1000.0",ret == 1000.0);
        //
        CORBA_RTCUtil.set_current_rate(m_conoutRef.value,0,500.0);
        ret = CORBA_RTCUtil.get_current_rate(m_conoutRef.value,0);
        assertTrue("test:get_rate 500.0",ret == 500.0);
        CORBA_RTCUtil.set_current_rate(m_conoutRef.value,1,500.0);
        ret = CORBA_RTCUtil.get_current_rate(m_conoutRef.value,1);
        assertTrue("test:get_rate 500.0",ret == 500.0);
        //
        //
        //
        ret = CORBA_RTCUtil.get_current_rate(null,0);
        assertTrue("test:",ret == -1.0);
        ret = CORBA_RTCUtil.get_current_rate(null,1);
        assertTrue("test:",ret == -1.0);
        ReturnCode_t code;
        code = CORBA_RTCUtil.set_current_rate(null, 0, 500.0);
        assertTrue("test:set_rate",code == ReturnCode_t.BAD_PARAMETER);
        code = CORBA_RTCUtil.set_current_rate(null, 1, 500.0);
        assertTrue("test:set_rate",code == ReturnCode_t.BAD_PARAMETER);
        //
        ret = CORBA_RTCUtil.get_current_rate(m_conoutRef.value,2);
        code = CORBA_RTCUtil.set_current_rate(m_conoutRef.value, 2, 500.0);
        assertTrue("test:set_rate",code == ReturnCode_t.BAD_PARAMETER);
  
        CORBA_RTCUtil.set_current_rate(m_conoutRef.value,0,1000.0);
        CORBA_RTCUtil.set_current_rate(m_conoutRef.value,1,1000.0);
    }
    /**
     *
     * add_rtc_to_default_ec
     * remove_rtc_to_default_ec
     * get_participants_rtc
     *
     */
    public void test_rtc_to_default_ec(){
        ReturnCode_t code;
        code =  CORBA_RTCUtil.add_rtc_to_default_ec(m_conoutRef.value, 
                m_serconRef.value);
        assertTrue("test:add_rtc",code == ReturnCode_t.RTC_OK);

        RTObject[] objs;
        objs = CORBA_RTCUtil.get_participants_rtc(m_conoutRef.value);
        //System.out.println( "length : "+ objs.length);
        assertTrue("test:get_rtc",objs.length == 1);

        code = CORBA_RTCUtil.remove_rtc_to_default_ec(m_conoutRef.value,
                m_serconRef.value);
        assertTrue("test:remove_rtc : "+code.value() ,code == ReturnCode_t.RTC_OK);

        objs = CORBA_RTCUtil.get_participants_rtc(m_conoutRef.value);
        //System.out.println( "length : "+ objs.length);
        assertTrue("test:get_rtc",objs.length == 0);

        //
        //
        //
        code =  CORBA_RTCUtil.add_rtc_to_default_ec(null, m_serconRef.value);
        assertTrue("test:add_rtc",code == ReturnCode_t.RTC_ERROR);
        code =  CORBA_RTCUtil.add_rtc_to_default_ec(m_conoutRef.value, null);
        assertTrue("test:add_rtc",code == ReturnCode_t.RTC_ERROR);
        code =  CORBA_RTCUtil.add_rtc_to_default_ec(null, null);
        assertTrue("test:add_rtc",code == ReturnCode_t.RTC_ERROR);
        //
        code = CORBA_RTCUtil.remove_rtc_to_default_ec(null, m_serconRef.value);
        assertTrue("test:remove_rtc",code == ReturnCode_t.RTC_ERROR);
        code = CORBA_RTCUtil.remove_rtc_to_default_ec(m_conoutRef.value,null);
        assertTrue("test:remove_rtc",code == ReturnCode_t.RTC_ERROR);
        code = CORBA_RTCUtil.remove_rtc_to_default_ec(null,null);
        assertTrue("test:remove_rtc",code == ReturnCode_t.RTC_ERROR);
        objs = CORBA_RTCUtil.get_participants_rtc(null);
        assertTrue("test:",objs == null);

    }
    /**
     *
     * get_port_names
     * get_inport_names
     * get_outport_names
     * get_svcport_names
     *
     */
    public void test_get_port_names(){
        Vector<String> names;
        //
        //
        //
        names = CORBA_RTCUtil.get_port_names(m_conoutRef.value);
        System.out.println( "names : "+ names.toString());
        assertTrue("test:",names.size() == 1);

        names = CORBA_RTCUtil.get_port_names(m_coninRef.value);
        System.out.println( "names : "+ names.toString());
        assertTrue("test:",names.size() == 1);

        names = CORBA_RTCUtil.get_port_names(m_serconRef.value);
        System.out.println( "names : "+ names.toString());
        assertTrue("test:",names.size() == 1);

        //
        //
        //
        names = CORBA_RTCUtil.get_inport_names(m_conoutRef.value);
        System.out.println( "names : "+ names.toString());
        assertTrue("test:",names.size() == 1);

        names = CORBA_RTCUtil.get_inport_names(m_coninRef.value);
        System.out.println( "names : "+ names.toString());
        assertTrue("test:",names.size() == 0);

        names = CORBA_RTCUtil.get_inport_names(m_serconRef.value);
        System.out.println( "names : "+ names.toString());
        assertTrue("test:",names.size() == 0);

        //
        //
        //
        names = CORBA_RTCUtil.get_outport_names(m_conoutRef.value);
        System.out.println( "names : "+ names.toString());
        assertTrue("test:",names.size() == 0);

        names = CORBA_RTCUtil.get_outport_names(m_coninRef.value);
        System.out.println( "names : "+ names.toString());
        assertTrue("test:",names.size() == 1);

        names = CORBA_RTCUtil.get_outport_names(m_serconRef.value);
        System.out.println( "names : "+ names.toString());
        assertTrue("test:",names.size() == 0);

        //
        //
        //
        names = CORBA_RTCUtil.get_svcport_names(m_conoutRef.value);
        System.out.println( "names : "+ names.toString());
        assertTrue("test:",names.size() == 0);

        names = CORBA_RTCUtil.get_svcport_names(m_coninRef.value);
        System.out.println( "names : "+ names.toString());
        assertTrue("test:",names.size() == 0);

        names = CORBA_RTCUtil.get_svcport_names(m_serconRef.value);
        System.out.println( "names : "+ names.toString());
        assertTrue("test:",names.size() == 1);


        names = CORBA_RTCUtil.get_port_names(null);
        assertTrue("test:",names == null);
        names = CORBA_RTCUtil.get_inport_names(null);
        assertTrue("test:",names == null);
        names = CORBA_RTCUtil.get_outport_names(null);
        assertTrue("test:",names == null);
        names = CORBA_RTCUtil.get_svcport_names(null);
        assertTrue("test:",names == null);
    }
    /**
     *
     * get_port_by_name
     *
     */
    public void test_get_port_by_name(){
        PortService ps;
        ps = CORBA_RTCUtil.get_port_by_name(m_conoutRef.value, 
                 m_conout_name+".in");
        assertTrue("test:",ps.get_port_profile().name.equals(m_conout_name+".in"));
        
        ps = CORBA_RTCUtil.get_port_by_name(null,
                 "ConsoleOut0.in");
        assertTrue("test:",ps == null);

        ps = CORBA_RTCUtil.get_port_by_name(m_conoutRef.value, "");
        assertTrue("test:",ps == null);
    }
    /**
     *
     * connect
     *
     */
    public void test_connect(){

        PortService port1 = CORBA_RTCUtil.get_port_by_name(m_conoutRef.value, 
                 m_conout_name + ".in");
        PortService port2 = CORBA_RTCUtil.get_port_by_name(m_coninRef.value, 
                 m_conin_name + ".out");
        Properties prop = new Properties();
        String[] conprop = {
            "dataport.interface_type","corba_cdr",
            "dataport.dataflow_type", "push",
            ""
        };
        
        prop.setDefaults(conprop);
        ReturnCode_t code;
        code = CORBA_RTCUtil.connect("kamo0",prop,port1,port2);
        assertTrue("test:connect",code == ReturnCode_t.RTC_OK);
        //
        //
        //Already connected
	//Because the default setting of dual connection is not allowed.
        code = CORBA_RTCUtil.connect("",prop,port1,port2);
        assertTrue("test:connect",code == ReturnCode_t.PRECONDITION_NOT_MET);
        code = CORBA_RTCUtil.connect("kamo1",null,port1,port2);
        assertTrue("test:connect",code == ReturnCode_t.PRECONDITION_NOT_MET);
        code = CORBA_RTCUtil.connect("kamo2",prop,null,port2);
        assertTrue("test:",code == ReturnCode_t.BAD_PARAMETER);
        code = CORBA_RTCUtil.connect("kamo3",prop,port1,null);
        assertTrue("test:",code == ReturnCode_t.BAD_PARAMETER);
 
        code = CORBA_RTCUtil.disconnect_by_portref_connector_name(
                port1, "kamo0");
        assertTrue("test:disconnect",code == ReturnCode_t.RTC_OK);
        code = CORBA_RTCUtil.disconnect_by_portref_connector_name(
                null, "kamo0");
        assertTrue("test:",code == ReturnCode_t.BAD_PARAMETER);
        code = CORBA_RTCUtil.disconnect_by_portref_connector_name(
                port1, "kamo5");
        assertTrue("test:"+code.value(),code == ReturnCode_t.BAD_PARAMETER);
    
    }
    /**
     *
     * connect
     *
     */
    public void test_connect_by_name(){

        PortService port1 = CORBA_RTCUtil.get_port_by_name(m_conoutRef.value, 
                 m_conout_name + ".in");
        PortService port2 = CORBA_RTCUtil.get_port_by_name(m_coninRef.value, 
                 m_conin_name + ".out");
        Properties prop = new Properties();
        String[] conprop = {
            "dataport.interface_type","corba_cdr",
            "dataport.dataflow_type", "push",
            ""
        };
        
        prop.setDefaults(conprop);
        ReturnCode_t code;
        code = CORBA_RTCUtil.connect_by_name("kamo0",prop,
                m_conoutRef.value,m_conout_name + ".in",
                m_coninRef.value,m_conin_name + ".out");
        assertTrue("test:connect",code == ReturnCode_t.RTC_OK);
        //
        //
        //
	port1.disconnect_all();
        code = CORBA_RTCUtil.connect_by_name("",prop,
                m_conoutRef.value,m_conout_name + ".in",
                m_coninRef.value,m_conin_name + ".out");
        assertTrue("test:connect",code == ReturnCode_t.RTC_OK);
	port1.disconnect_all();
        code = CORBA_RTCUtil.connect_by_name("kamo1",null,
                m_conoutRef.value,m_conout_name + ".in",
                m_coninRef.value,m_conin_name + ".out");
        assertTrue("test:connect",code == ReturnCode_t.RTC_OK);
        code = CORBA_RTCUtil.connect_by_name("kamo2",prop,
                null,"ConsoleOut0.in",
                m_coninRef.value,"ConsoleIn0.out");
        assertTrue("test:",code == ReturnCode_t.BAD_PARAMETER);
        code = CORBA_RTCUtil.connect_by_name("kamo3",prop,
                m_conoutRef.value,"ConsoleOut0.in",
                m_coninRef.value,"");
        assertTrue("test:",code == ReturnCode_t.BAD_PARAMETER);
        
        
	port1.disconnect_all();
        code = CORBA_RTCUtil.connect_by_name("kamo0",prop,
                m_conoutRef.value,m_conout_name + ".in",
                m_coninRef.value,m_conin_name + ".out");
        ConnectorProfile[] cprofs = port1.get_connector_profiles();
        for(int ic=0;ic<cprofs.length;++ic){
            if(cprofs[ic].name.equals("kamo0")){
                code = CORBA_RTCUtil.disconnect(cprofs[ic]);
                break;
            }
        }
        
        assertTrue("test:disconnect "+code.value(),code == ReturnCode_t.RTC_OK);
        code = CORBA_RTCUtil.disconnect(null);
        assertTrue("test:",code == ReturnCode_t.BAD_PARAMETER);
    }
    /**
     *
     * disconnect
     *
     */
    public void test_disconnect_by_id(){

        PortService port1 = CORBA_RTCUtil.get_port_by_name(m_conoutRef.value, 
                 m_conout_name + ".in");
        PortService port2 = CORBA_RTCUtil.get_port_by_name(m_coninRef.value, 
                 m_conin_name + ".out");
        Properties prop = new Properties();
        String[] conprop = {
            "dataport.interface_type","corba_cdr",
            "dataport.dataflow_type", "push",
            ""
        };
        
        prop.setDefaults(conprop);
        ReturnCode_t code;
        code = CORBA_RTCUtil.connect("kamo0",prop,port1,port2);
        assertTrue("test:connect",code == ReturnCode_t.RTC_OK);
        //
        //
        //
        String id = new String(); 
        ConnectorProfile[] cprofs = port1.get_connector_profiles();
        for(int ic=0;ic<cprofs.length;++ic){
            if(cprofs[ic].name.equals("kamo0")){
                id = cprofs[ic].connector_id;
                break;
            }
        }
        code = CORBA_RTCUtil.disconnect_by_portref_connector_id(port1,id);
        assertTrue("test:disconnect",code == ReturnCode_t.RTC_OK);



        code = CORBA_RTCUtil.disconnect_by_portref_connector_id(null,id);
        assertTrue("test:",code == ReturnCode_t.BAD_PARAMETER);
        code = CORBA_RTCUtil.disconnect_by_portref_connector_id(port1,"");
        assertTrue("test:"+code.value(),code == ReturnCode_t.BAD_PARAMETER);
    
    }
    /**
     *
     * disconnect
     *
     */
    public void test_disconnect_by_port_name(){
        PortService port1 = CORBA_RTCUtil.get_port_by_name(m_conoutRef.value, 
                 m_conout_name + ".in");
        PortService port2 = CORBA_RTCUtil.get_port_by_name(m_coninRef.value, 
                 m_conin_name + ".out");
        Properties prop = new Properties();
        String[] conprop = {
            "dataport.interface_type","corba_cdr",
            "dataport.dataflow_type", "push",
            ""
        };
        
        prop.setDefaults(conprop);
        ReturnCode_t code;
        code = CORBA_RTCUtil.connect("kamo0",prop,port1,port2);
        assertTrue("test:connect",code == ReturnCode_t.RTC_OK);




        code = CORBA_RTCUtil.disconnect_by_port_name(port1,m_conin_name + ".out");
        assertTrue("test:disconnect",code == ReturnCode_t.RTC_OK);

        code = CORBA_RTCUtil.disconnect_by_port_name(port1,"");
        assertTrue("test:",code == ReturnCode_t.BAD_PARAMETER);
        code = CORBA_RTCUtil.disconnect_by_port_name(null,m_conin_name + ".out");
        assertTrue("test:",code == ReturnCode_t.BAD_PARAMETER);

    }
    /**
     *
     * get_parameter_by_key
     * get_current_configuration_name
     * get_active_configuration
     * set_configuration
     */
    public void test_get_parameter_by_key(){
        String str;
        str = CORBA_RTCUtil.get_parameter_by_key(m_configRef.value,
                "default","double_param0");
        assertTrue("test:",str.equals("0.11"));
    
        str = CORBA_RTCUtil.get_parameter_by_key(m_configRef.value,
                "","double_param0");
        assertTrue("test:",str.equals(""));
        str = CORBA_RTCUtil.get_parameter_by_key(m_configRef.value,
                "default","");
        assertTrue("test:",str.equals(""));



        str = CORBA_RTCUtil.get_active_configuration_name(m_configRef.value);
        assertTrue("test:",str.equals("default"));
        str = CORBA_RTCUtil.get_active_configuration_name(null);
        assertTrue("test:",str.equals(""));



        Properties prop 
            = CORBA_RTCUtil.get_active_configuration(m_configRef.value);
        str = prop.getProperty("double_param0");
        assertTrue("test:",str.equals("0.11"));
        prop = CORBA_RTCUtil.get_active_configuration(null);
        assertTrue("test:",prop==null);
    
        boolean bool = CORBA_RTCUtil.set_configuration(m_configRef.value, 
                "default", "double_param0","305.8560");
        assertTrue("test:",bool);
        str = CORBA_RTCUtil.get_parameter_by_key(m_configRef.value,
                "default","double_param0");
        assertTrue("test:",str.equals("305.8560"));
        //
        bool = CORBA_RTCUtil.set_configuration(null, 
                "default", "double_param0","305.8560");
        assertTrue("test:",!bool);
        bool = CORBA_RTCUtil.set_configuration(m_configRef.value, 
                "", "double_param0","305.8560");
        assertTrue("test:",!bool);
        bool = CORBA_RTCUtil.set_configuration(m_configRef.value, 
                "default", "double_param2","123.456");
        assertTrue("test:",bool);
        str = CORBA_RTCUtil.get_parameter_by_key(m_configRef.value,
                "default","double_param2");
        assertTrue("test:",str.equals("123.456"));


    }
    /**
     *
     * connect
     *
     */
    public void test_connect_multi(){

        PortService port1 = CORBA_RTCUtil.get_port_by_name(m_conoutRef.value, 
                 m_conout_name + ".in");
        PortService port2 = CORBA_RTCUtil.get_port_by_name(m_coninRef.value, 
                 m_conin_name + ".out");
        PortService port3 = CORBA_RTCUtil.get_port_by_name(m_coninRef2.value, 
                 m_conin2_name + ".out");
        Properties prop = new Properties();
        String[] conprop = {
            "dataport.interface_type","corba_cdr",
            "dataport.dataflow_type", "push",
            "dataport.allow_dup_connection:yes",
            ""
        };
        PortServiceListHolder target_ports = new PortServiceListHolder();
        target_ports.value = new PortService[2];
        target_ports.value[0] = port2;
        target_ports.value[1] = port3;
        prop.setDefaults(conprop);
        ReturnCode_t code;
        code = CORBA_RTCUtil.connect_multi("kamo10", prop, 
                    port1,  target_ports);
        assertTrue("test:",code == ReturnCode_t.RTC_OK);
        code = CORBA_RTCUtil.connect_multi("kamo11", prop, 
                    null,  target_ports);
        assertTrue("test:",code == ReturnCode_t.BAD_PARAMETER);
        code = CORBA_RTCUtil.connect_multi("kamo11", prop, 
                    port1,  null);
        assertTrue("test:",code == ReturnCode_t.BAD_PARAMETER);
        PortServiceListHolder error_ports = new PortServiceListHolder();
        code = CORBA_RTCUtil.connect_multi("kamo11", prop, 
                    port1, error_ports );
        assertTrue("test:",code == ReturnCode_t.BAD_PARAMETER);
    }

    /**
     *
     * connect
     *
     */
    public void test_disconnect_by_portname_connector_name(){

        PortService port1 = CORBA_RTCUtil.get_port_by_name(m_conoutRef.value, 
                 m_conout_name + ".in");
        PortService port2 = CORBA_RTCUtil.get_port_by_name(m_coninRef.value, 
                 m_conin_name + ".out");
	port1.disconnect_all();
        Properties prop = new Properties();
        String[] conprop = {
            "dataport.interface_type","corba_cdr",
            "dataport.dataflow_type", "push",
            ""
        };
        
        prop.setDefaults(conprop);
        ReturnCode_t code;
        code = CORBA_RTCUtil.connect("kamo0",prop,port1,port2);
        assertTrue("test:connect",code == ReturnCode_t.RTC_OK);

        code = CORBA_RTCUtil.disconnect_by_portname_connector_name(
                "rtcname://localhost:2809/"+ m_conout_name+".in", "kamo0");
        assertTrue("test:disconnect",code == ReturnCode_t.RTC_OK);

        //code = CORBA_RTCUtil.connect("kamo0",prop,port1,port2);
        //assertTrue("test:connect",code == ReturnCode_t.RTC_OK);
        //code = CORBA_RTCUtil.disconnect_by_portref_connector_name(
        //        port1, "kamo0");

        //assertTrue("test:disconnect",code == ReturnCode_t.RTC_OK);

        code = CORBA_RTCUtil.disconnect_by_portref_connector_name(
                null, "kamo0");
        assertTrue("test:",code == ReturnCode_t.BAD_PARAMETER);
        code = CORBA_RTCUtil.disconnect_by_portref_connector_name(
                port1, "kamo5");
        assertTrue("test:"+code.value(),code == ReturnCode_t.BAD_PARAMETER);

    
    }
    /**
     *
     * connect
     *
     */
    public void test_connect_by_name2(){

        PortService port1 = CORBA_RTCUtil.get_port_by_name(m_conoutRef.value, 
                 m_conout_name + ".in");
        PortService port2 = CORBA_RTCUtil.get_port_by_name(m_coninRef.value, 
                 m_conin_name + ".out");
        Properties prop = new Properties();
        String[] conprop = {
            "dataport.interface_type","corba_cdr",
            "dataport.dataflow_type", "push",
            ""
        };
        
        prop.setDefaults(conprop);
        ReturnCode_t code;
        code = CORBA_RTCUtil.connect_by_name("kamo0",prop,
                m_conoutRef.value,m_conout_name + ".in",
                m_coninRef.value,m_conin_name + ".out");
        assertTrue("test:connect",code == ReturnCode_t.RTC_OK);
        //
        //
        //
	port1.disconnect_all();
        code = CORBA_RTCUtil.connect_by_name("",prop,
                m_conoutRef.value,m_conout_name + ".in",
                m_coninRef.value,m_conin_name + ".out");
        assertTrue("test:disconnect",code == ReturnCode_t.RTC_OK);
	port1.disconnect_all();
        code = CORBA_RTCUtil.connect_by_name("kamo1",null,
                m_conoutRef.value,m_conout_name + ".in",
                m_coninRef.value,m_conin_name + ".out");
        assertTrue("test:disconnect",code == ReturnCode_t.RTC_OK);
	port1.disconnect_all();
        code = CORBA_RTCUtil.connect_by_name("kamo2",prop,
                null,m_conout_name + ".in",
                m_coninRef.value,m_conin_name + ".out");
        assertTrue("test:",code == ReturnCode_t.BAD_PARAMETER);
        code = CORBA_RTCUtil.connect_by_name("kamo3",prop,
                m_conoutRef.value,m_conout_name + ".in",
                m_coninRef.value,"");
        assertTrue("test:",code == ReturnCode_t.BAD_PARAMETER);
        
        
        code = CORBA_RTCUtil.connect_by_name("kamo0",prop,
                m_conoutRef.value,m_conout_name + ".in",
                m_coninRef.value,m_conin_name + ".out");
        ConnectorProfile[] cprofs = port1.get_connector_profiles();
        for(int ic=0;ic<cprofs.length;++ic){
            if(cprofs[ic].name.equals("kamo0")){
                code = CORBA_RTCUtil.disconnect(cprofs[ic]);
                break;
            }
        }
        
        assertTrue("test:disconnect "+code.value(),code == ReturnCode_t.RTC_OK);
        code = CORBA_RTCUtil.disconnect(null);
        assertTrue("test:",code == ReturnCode_t.BAD_PARAMETER);
    }



    public static String console_in_conf[] = {
            "implementation_id", "ConsoleIn",
            "type_name",         "ConsoleIn",
            "description",       "Console input component",
            "version",           "1.0",
            "vendor",            "Noriaki Ando, AIST",
            "category",          "example",
            "activity_type",     "DataFlowComponent",
            "max_instance",      "10",
            "language",          "Java",
            "lang_type",         "compile",
            ""
    };
    private class ConsoleIn implements RtcNewFunc, RtcDeleteFunc, RegisterModuleFunc {


        public RTObject_impl createRtc(Manager mgr) {
            return new ConsoleInImpl(mgr);
        }

        public void deleteRtc(RTObject_impl rtcBase) {
            rtcBase = null;
        }
        public void registerModule() {
            Properties prop = new Properties(console_in_conf);
            final Manager manager = Manager.instance();
            manager.registerFactory(prop, new ConsoleIn(), new ConsoleIn());
        }
    }
    public static String console_out_conf[] = {
            "implementation_id", "ConsoleOut",
            "type_name",         "ConsoleOut",
            "description",       "Console output component",
            "version",           "1.0",
            "vendor",            "Noriaki Ando, AIST",
            "category",          "example",
            "activity_type",     "DataFlowComponent",
            "max_instance",      "10",
            "language",          "Java",
            "lang_type",         "compile",
            ""
    };
    private class ConsoleOut implements RtcNewFunc, RtcDeleteFunc, RegisterModuleFunc {


        public RTObject_impl createRtc(Manager mgr) {
            return new ConsoleOutImpl(mgr);
        }

        public void deleteRtc(RTObject_impl rtcBase) {
            rtcBase = null;
        }
        public void registerModule() {
            Properties prop = new Properties(console_out_conf);
            final Manager manager = Manager.instance();
            manager.registerFactory(prop, new ConsoleOut(), new ConsoleOut());
        }
    }
    public static String consumer_conf[] = {
            "implementation_id", "MyServiceConsumer",
            "type_name",         "MyServiceConsumer",
            "description",       "MyService Consumer Sample component",
            "version",           "0.1",
            "vendor",            "AIST",
            "category",          "Generic",
            "activity_type",     "DataFlowComponent",
            "max_instance",      "10",
            "language",          "Java",
            "lang_type",         "compile",
            ""
    };

    private class MyServiceConsumer implements RtcNewFunc, RtcDeleteFunc {

        public RTObject_impl createRtc(Manager mgr) {
            return new MyServiceConsumerImpl(mgr);
        }

        public void deleteRtc(RTObject_impl rtcBase) {
            rtcBase = null;
        }

        public void registerModule() {
            Properties prop = new Properties(consumer_conf);
            final Manager manager = Manager.instance();
            manager.registerFactory(prop, 
                                new MyServiceConsumer(), 
                                new MyServiceConsumer());
        }
    }
    public static String conf_sample_conf[] = {
            "implementation_id", "ConfigSample",
            "type_name",         "ConfigSample",
            "description",       "Configuration example component",
            "version",           "1.0",
            "vendor",            "Noriaki Ando, AIST",
            "category",          "example",
            "activity_type",     "DataFlowComponent",
            "max_instance",      "10",
            "language",          "Java",
            "lang_type",         "compile",
            // Configuration variables
            "conf.default.int_param0", "0",
            "conf.default.int_param1", "1",
            "conf.default.double_param0", "0.11",
            "conf.default.double_param1", "9.9",
            "conf.default.str_param0", "hoge",
            "conf.default.str_param1", "dara",
            "conf.default.vector_param0", "0.0,1.0,2.0,3.0,4.0",
            ""
    };
    private class ConfigSample implements RtcNewFunc, RtcDeleteFunc {

        public RTObject_impl createRtc(Manager mgr) {
            return new ConfigSampleImpl(mgr);
        }

        public void deleteRtc(RTObject_impl rtcBase) {
            rtcBase = null;
        }
        public void registerModule() {
            Properties prop = new Properties(conf_sample_conf);
            final Manager manager = Manager.instance();
            manager.registerFactory(prop, new ConfigSample(), new ConfigSample());
        }
    }

    private class ConsoleInImpl extends DataFlowComponentBase {

        public ConsoleInImpl(Manager manager) {
            super(manager);
            m_out_val = new TimedLong(new RTC.Time(0,0),0);
            m_out = new DataRef<TimedLong>(m_out_val);
            m_outOut = new OutPort<TimedLong>("out", m_out);
        }

        @Override
        protected ReturnCode_t onInitialize() {

            try {
                addOutPort("out", m_outOut);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return super.onInitialize();
        }
        @Override
        protected ReturnCode_t onExecute(int ec_id) {
            System.out.println("Please input number: ");
            BufferedReader buff = new BufferedReader(new InputStreamReader( System.in ));
            try {
                String str = buff.readLine();
                if(str != null){
                    m_out_val.data = Integer.parseInt(str);
                }
            } catch (NumberFormatException e) {
                System.out.println("Input number Error!");
            } catch (IOException e) {
                System.out.println("Input number Error!");
            }
            System.out.println("Sending to subscriber: "  + m_out_val.data);
            m_outOut.write();

            return super.onExecute(ec_id);
        }
        // DataOutPort declaration
        // <rtc-template block="outport_declare">
        protected TimedLong m_out_val;
        protected DataRef<TimedLong> m_out;
        protected OutPort<TimedLong> m_outOut;
    
    }

    private class ConsoleOutImpl  extends DataFlowComponentBase {

        public ConsoleOutImpl(Manager manager) {
            super(manager);
            m_in_val = new TimedLong();
            m_in = new DataRef<TimedLong>(m_in_val);
            m_inIn = new InPort<TimedLong>("in", m_in);
        }

        @Override
        protected ReturnCode_t onInitialize() {
            try {
                addInPort("in", m_inIn);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return super.onInitialize();
        }
        @Override
        protected ReturnCode_t onExecute(int ec_id) {
            if( m_inIn.isNew() ) {
                m_inIn.read();
                System.out.print( "Received: " + m_in.v.data + " " );
                System.out.print( "TimeStamp: " + m_in.v.tm.sec + "[s] " );
                System.out.println( m_in.v.tm.nsec + "[ns]" );
            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return super.onExecute(ec_id);
        }
        protected TimedLong m_in_val;
        protected DataRef<TimedLong> m_in;
        protected InPort<TimedLong> m_inIn;
    }

    private class ConfigSampleImpl extends DataFlowComponentBase {

        public ConfigSampleImpl(Manager manager) {
            super(manager);
        }

        @Override
        protected ReturnCode_t onInitialize() {
            // Bind variables and configuration variable
            bindParameter("int_param0", m_int_param0, "0");
            bindParameter("int_param1", m_int_param1, "1");
            bindParameter("double_param0", m_double_param0, "0.11");
            bindParameter("double_param1", m_double_param1, "9.9");
            bindParameter("str_param0", m_str_param0, "hoge");
            bindParameter("str_param1", m_str_param1, "dara");
            bindParameter("vector_param0", m_vector_param0, "0.0,1.0,2.0,3.0,4.0");
        

            System.out.println("");
            System.out.println("Please change configuration values from RtcLink"); 
            System.out.println("");

            return super.onInitialize();
        }
        @Override
        protected ReturnCode_t onExecute(int ec_id) {

            int maxlen = 0;
            int curlen = 0;
            final String c = "                    ";
            if( true ) {
                System.out.println( "---------------------------------------" );
                System.out.println( " Active Configuration Set: " );
                System.out.println( m_configsets.getActiveId() + c );
                System.out.println(  "---------------------------------------" );
            
                System.out.println( "int_param0:       " + m_int_param0 + c );
                System.out.println( "int_param1:       " + m_int_param1 + c );
                System.out.println( "double_param0:    " + m_double_param0 + c );
                System.out.println( "double_param1:    " + m_double_param1 + c );
                System.out.println( "str_param0:       " + m_str_param0 + c );
                System.out.println( "str_param1:       " + m_str_param1 + c );
                for( int intIdx=0;intIdx<m_vector_param0.value.size();++intIdx ) {
                    System.out.println( "vector_param0[" + intIdx + "]: " + m_vector_param0.value.elementAt(intIdx) + c );
                }
                System.out.println( "---------------------------------------" );
    
                curlen = m_vector_param0.value.size();
                if( curlen >= maxlen ) maxlen = curlen;
                for( int intIdx=0;intIdx<maxlen-curlen;++intIdx ) {
                    System.out.println( c + c );
                }
                System.out.println( "Updating.... " + ticktack() + c );
    
                for( int intIdx=0;intIdx<11+maxlen;++intIdx ) {
    //          std::cout << "[A\r";
                }
            }
            if( m_int_param0.value>1000 && m_int_param0.value<1000000 ) {
                try {
                    Thread.sleep(0, m_int_param0.value);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return super.onExecute(ec_id);
        }
        protected IntegerHolder m_int_param0 = new IntegerHolder();
        protected IntegerHolder m_int_param1 = new IntegerHolder();
        protected DoubleHolder m_double_param0 = new DoubleHolder();
        protected DoubleHolder m_double_param1 = new DoubleHolder();
        protected StringHolder m_str_param0 = new StringHolder();
        protected StringHolder m_str_param1 = new StringHolder();
        protected VectorHolder m_vector_param0 = new VectorHolder();

        private int index = 0;
        private char c[] = {'/', '-', '|','-'}; 
        private char ticktack() {
            index = (++index) % 4;
            return c[index];
        }
    }
    private class MyServiceConsumerImpl  extends DataFlowComponentBase {

        public MyServiceConsumerImpl(Manager manager) {
            super(manager);
            m_MyServicePort = new CorbaPort("MyService");
            this.result = new DataRef<String>(resultVal);
        }

        @Override
        protected RTC.ReturnCode_t onInitialize() {
            m_MyServicePort.registerConsumer("myservice0", 
                                            "MyService", 
                                            m_myservice0Base);
            // Set CORBA Service Ports
            addPort(m_MyServicePort);
            return super.onInitialize();
        }
        @Override
        protected ReturnCode_t onExecute(int ec_id) {
            try {
                System.out.println("");
                System.out.println("Command list: ");
                System.out.println(" echo [msg]       : echo message.");
                System.out.println(" set_value [value]: set value." );
                System.out.println(" get_value        : get current value.");
                System.out.println(" get_echo_history : get input messsage history." );
                System.out.println(" get_value_history: get input value history." );
                System.out.print("> ");
          
                String args = null;
                int pos;
                String argv[] = null;
//              std::getline(std::cin, args);
                BufferedReader buff = new BufferedReader(new InputStreamReader( System.in ));
                try {
                    args = buff.readLine();
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(args == null){
                    return super.onExecute(ec_id);
                }
          
                pos = args.indexOf(" ");
                if( pos>0 ) {
                    argv = new String[2];
                    argv[0] = args.substring(0,pos);
                    argv[1] = args.substring(pos+1);
                } else {
                    argv = new String[1];
                    argv[0] = args;
                }
                if(async != null && !async.isAlive()){
                System.out.println( "echo() finished: " + this.result.v );
                async = null;
            }
            m_myservice0 = m_myservice0Base._ptr();
            if( argv[0].equals("echo") && argv.length>1 ) {
                if(async == null){
                  //String retmsg = m_myservice0.echo(argv[1]);
                  //System.out.println( "echo return: " + retmsg );
                    async = new Thread(
                          new echoFunctor(m_myservice0,argv[1],this.result));
                    async.start();
                } else{
                    System.out.println("echo() still invoking");
                }
                return super.onExecute(ec_id);
            }
          
            if( argv[0].equals("set_value") && argv.length>1 ) {
                Float val = Float.valueOf(argv[1]);
                m_myservice0.set_value(val.floatValue());
                System.out.println( "Set remote value: " + val );
                return super.onExecute(ec_id);
            }
          
            if( argv[0].equals("get_value") ) {
                System.out.println( "Current remote value: " + m_myservice0.get_value() );
                return super.onExecute(ec_id);
            }  
          
            if( argv[0].equals("get_echo_history") ) {
                String[] echo_history = m_myservice0.get_echo_history();
                for( int intIdx=0;intIdx<echo_history.length;intIdx++ ) {
                    System.out.println( intIdx+": "+echo_history[intIdx]);
                }
                return super.onExecute(ec_id);
            }  
          
            if( argv[0].equals("get_value_history") ) {
                float[] value_history = m_myservice0.get_value_history();
                for( int intIdx=0;intIdx<value_history.length;intIdx++ ) {
                    System.out.println( intIdx+": "+value_history[intIdx]);
                }
                return super.onExecute(ec_id);
            }
                System.out.println( "Invalid command or argument(s)." );
            } catch (Exception ex) {
                 System.out.println( "No service connected." );
            }
            return super.onExecute(ec_id);
        }
        protected CorbaPort m_MyServicePort;
        protected CorbaConsumer<MyService> m_myservice0Base =
            new CorbaConsumer<MyService>(MyService.class);
    
        protected MyService m_myservice0;
    
        // </rtc-template>
        Thread async;
        String resultVal = new String();
        DataRef<String> result;

        class echoFunctor implements Runnable{
            public echoFunctor(MyService comp,String arg,DataRef<String> result){
                this.obj = comp;
                this.arg = arg;
                this.result = result;
            }
    
            @Override
            public void run() {
                result.v = obj.echo(this.arg);
            }
            MyService obj;  
            String arg;
            DataRef<String> result;
        }
    }

    private class VectorHolder  implements ValueHolder, Serializable {
        public Vector value = null;
        public VectorHolder() {
        }  
        public VectorHolder(Vector initialValue) {
            value = new Vector(initialValue);
        }
        public void stringFrom(String def_val) throws Exception {
            value = new Vector();
            String values[] = def_val.split(",");
            for( int intIdx=0;intIdx<values.length;intIdx++ ) {
                value.add(values[intIdx]);
            }
        }
        public Vector getValue(){
            return value;
        }
        public String toString(){
            StringBuffer retVal = new StringBuffer();
            while(value.iterator().hasNext()) {
                retVal.append(value.iterator().next());
                if(value.iterator().hasNext()) retVal.append("'");
            }
            return retVal.toString();
        }
    }

    private interface MyService extends MyServiceOperations, org.omg.CORBA.Object, org.omg.CORBA.portable.IDLEntity 
    {
    }
    private interface MyServiceOperations {
        String echo (String msg);
        String[] get_echo_history ();
        void set_value (float value);
        float get_value ();
        float[] get_value_history ();
    }

}
  

