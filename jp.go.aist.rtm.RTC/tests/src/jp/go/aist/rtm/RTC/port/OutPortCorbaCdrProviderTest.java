package jp.go.aist.rtm.RTC.port;

import junit.framework.TestCase;

import org.omg.CORBA.ORB;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.PortableServer.POA;

import java.util.logging.FileHandler;

import _SDOPackage.NVListHolder;
import RTC.ConnectorProfileHolder;

import jp.go.aist.rtm.RTC.BufferFactory;
import jp.go.aist.rtm.RTC.PeriodicTask;
import jp.go.aist.rtm.RTC.buffer.BufferBase;
import jp.go.aist.rtm.RTC.buffer.RingBuffer;
import jp.go.aist.rtm.RTC.buffer.CdrRingBuffer;
import jp.go.aist.rtm.RTC.util.CORBA_SeqUtil;
import jp.go.aist.rtm.RTC.util.NVUtil;
import jp.go.aist.rtm.RTC.util.DataRef;
import jp.go.aist.rtm.RTC.util.ORBUtil;
import jp.go.aist.rtm.RTC.util.NVListHolderFactory;
import jp.go.aist.rtm.RTC.log.Logbuf;
import jp.go.aist.rtm.RTC.port.ConnectorListener;
import jp.go.aist.rtm.RTC.util.Properties;

public class OutPortCorbaCdrProviderTest extends TestCase {
    /**
     * 
     * 
     */
    class OutPortCorbaCdrProviderMock extends OutPortCorbaCdrProvider {
        /**
         * 
         * 
         */
        public OutPortCorbaCdrProviderMock() {
        }
        /**
         *  for check
         */
        _SDOPackage.NVListHolder get_m_properties() {
            return m_properties;
        }
    };
    class Listener extends ConnectorListener{
        public Listener(final String name){
            m_name = name;
        }

        public jp.go.aist.rtm.RTC.connectorListener.ReturnCode operator(ConnectorBase.ConnectorInfo arg){
            return jp.go.aist.rtm.RTC.connectorListener.ReturnCode.NO_CHANGE;
        }
        public String m_name;
    }

    protected static Logbuf rtcout = null; 
    protected FileHandler m_fh;
    private ORB m_orb;
    private POA m_poa;
    /**
     * <p> Test initialization </p>
     */
    protected void setUp() throws Exception {
        super.setUp();
        java.util.Properties props = new java.util.Properties();
        this.m_orb = ORBUtil.getOrb();
        this.m_poa = org.omg.PortableServer.POAHelper.narrow(
                this.m_orb.resolve_initial_references("RootPOA"));
        this.m_poa.the_POAManager().activate();
        if(rtcout == null){
            rtcout = new Logbuf("Manager");
            m_fh = null; 
            rtcout.setLevel("SILENT");
            String logfile = "OutPortCorbaCdrProvider.log";
            m_fh = new FileHandler(logfile);
            rtcout.addStream(m_fh);
            rtcout.setLevel("PARANOID");

        }
    }
    /**
     * <p> Test finalization </p>
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        if( m_orb != null) {
            m_orb.destroy();
            m_orb = null;
        }
        rtcout.removeStream(m_fh);
        rtcout.removeStreamAll();
    }
    /**
     *  
     */
    protected OutputStream toStream(byte[] data, int sec, int nsec){
            java.util.Properties props = new java.util.Properties();
            props.put("org.omg.CORBA.ORBInitialPort", "2809");
            props.put("org.omg.CORBA.ORBInitialHost", "localhost");
            ORB orb = ORB.init(new String[0], props);
            OutputStream cdr
                = new EncapsOutputStreamExt(orb,true);
            RTC.Time tm = new RTC.Time(sec,nsec);
            RTC.TimedOctetSeq tmlong = new RTC.TimedOctetSeq(tm,data);
            RTC.TimedOctetSeqHolder tmlongholder 
                = new RTC.TimedOctetSeqHolder(tmlong);
            tmlongholder._write(cdr);
            return cdr;

    }
    /**
     *  
     */
    protected OutputStream toStream(int data, int sec, int nsec){
            org.omg.CORBA.Any any = ORBUtil.getOrb().create_any();
            OutputStream cdr = any.create_output_stream();
            RTC.Time tm = new RTC.Time(sec,nsec);
            RTC.TimedLong tmlong = new RTC.TimedLong(tm,data);
            RTC.TimedLongHolder tmlongholder 
                = new RTC.TimedLongHolder(tmlong);
            tmlongholder._write(cdr);
            return cdr;

    }
    /**
     * 
     */
    public void test_case0() {

        rtcout.println(rtcout.TRACE, "test_case0");
        //
        //
        //
        OutPortCorbaCdrProviderMock provider = 
            new OutPortCorbaCdrProviderMock();

        int index;
        //
        index = NVUtil.find_index(provider.get_m_properties(),
                                   "dataport.corba_cdr.outport_ior");
        assertTrue("1",0<=index);

        //
        index = NVUtil.find_index(provider.get_m_properties(),
                                   "dataport.corba_cdr.outport_ref");
        assertTrue("2",0<=index);


        //init() function is not implemented. 
        //provider.init();

         
        //RingBuffer<InputStream> buffer;
        RingBuffer<OutputStream> buffer;
        //final BufferFactory<RingBuffer<InputStream>,String> factory 
        final BufferFactory<RingBuffer<OutputStream>,String> factory 
            = BufferFactory.instance();
        factory.addFactory("ring_buffer",
                    new CdrRingBuffer(),
                    new CdrRingBuffer());
        buffer = factory.createObject("ring_buffer");

        OpenRTM.PortStatus retcode = null;


        RTC.ConnectorProfile prof = new RTC.ConnectorProfile();
        prof.connector_id = "id0";
        prof.name = "InPortBaseTest0";
        prof.ports = new RTC.PortService[1];

        NVListHolder nvholder = new NVListHolder(prof.properties);
        CORBA_SeqUtil.push_back(nvholder,
                                 NVUtil.newNV("dataport.interface_type",
                                 "corba_cdr"));
        CORBA_SeqUtil.push_back(nvholder,
                                 NVUtil.newNV("dataport.dataflow_type",
                                 "pull"));
        CORBA_SeqUtil.push_back(nvholder,
                                 NVUtil.newNV("dataport.subscription_type",
                                 "new"));
        prof.properties = nvholder.value;
        ConnectorProfileHolder cprof =  new ConnectorProfileHolder(prof);
        Properties prop = new Properties();
        ConnectorBase.ConnectorInfo profile
            = new ConnectorBase.ConnectorInfo(cprof.value.name,
                                 cprof.value.connector_id,
                                 CORBA_SeqUtil.refToVstring(cprof.value.ports),
                                 prop);
        ConnectorListeners listeners = new ConnectorListeners();
        listeners.connectorData_[ConnectorListenerType.ON_SENDER_ERROR].addObserver(new Listener("test"));
        provider.setListener(profile, listeners);

        byte[] cdr_data = new byte[256];
        OpenRTM.CdrDataHolder cdr_data_ref = new OpenRTM.CdrDataHolder(cdr_data);

        //get() is called without setting the buffer.
        retcode = provider.get(cdr_data_ref);
        assertEquals("3",OpenRTM.PortStatus.UNKNOWN_ERROR, retcode);
        
        {
        try {
            OutPortConnector connector = new OutPortPullConnector(profile,
                                                provider,
                                                listeners);
            provider.setConnector(connector);
        }
        catch (Exception e) {
        }
        }
        Properties props = new Properties();
        props.setProperty("read.empty_policy","do_nothing");
        buffer.init(props);
        provider.setBuffer(buffer);

        //get() is called without setting data. (empty)
        retcode = provider.get(cdr_data_ref);
        assertEquals("4",OpenRTM.PortStatus.BUFFER_EMPTY, retcode);

        byte testdata[] = { 4, 8, 15, 16, 23, 42, 49, 50};
        OutputStream cdr = toStream(testdata, 0, 0);
        buffer.write(cdr);
        provider.setBuffer(buffer);

        retcode = provider.get(cdr_data_ref);
        assertEquals("5",OpenRTM.PortStatus.PORT_OK, retcode);

        java.util.Properties uprops = new java.util.Properties();
        uprops.put("org.omg.CORBA.ORBInitialPort", "2809");
        uprops.put("org.omg.CORBA.ORBInitialHost", "localhost");
        ORB orb = ORB.init(new String[0], uprops);
        OutputStream ostream
                = new EncapsOutputStreamExt(orb,true);
        ostream.write_octet_array(cdr_data_ref.value,0,cdr_data_ref.value.length);
        
        RTC.TimedOctetSeqHolder holder 
              = new RTC.TimedOctetSeqHolder();
        holder._read(ostream.create_input_stream());

        
        for(int ic=0;ic<testdata.length;++ic){
            assertEquals("6:"+ic+":",holder.value.data[ic],testdata[ic]);
        }
        

        _SDOPackage.NVListHolder list = provider.get_m_properties();
        {
        index = NVUtil.find_index(list,
                                   "dataport.corba_cdr.outport_ior");
        }

        String ior = null;
        try {
            ior = list.value[index].value.extract_wstring();
            org.omg.CORBA.Object var = m_orb.string_to_object(ior);
            org.omg.PortableServer.Servant ser 
                     = m_poa.reference_to_servant(var);
	    m_poa.deactivate_object(m_poa.servant_to_id(ser));
        }
        catch( org.omg.PortableServer.POAPackage.WrongAdapter e){
        }
        catch( org.omg.PortableServer.POAPackage.WrongPolicy e){
        }
        catch( org.omg.PortableServer.POAPackage.ServantNotActive e){
        }
        catch( org.omg.PortableServer.POAPackage.ObjectNotActive e){
        }
        catch(org.omg.CORBA.BAD_OPERATION e){
        }
            
    }
}

