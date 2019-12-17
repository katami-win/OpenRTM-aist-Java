package jp.go.aist.rtm.RTC.port;

import junit.framework.TestCase;
import java.util.Vector;

import org.omg.CORBA.ORB;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.PortableServer.POA;

import _SDOPackage.NVListHolder;

import jp.go.aist.rtm.RTC.BufferFactory;
import jp.go.aist.rtm.RTC.buffer.BufferBase;
import jp.go.aist.rtm.RTC.buffer.RingBuffer;
import jp.go.aist.rtm.RTC.buffer.CdrRingBuffer;
import jp.go.aist.rtm.RTC.port.publisher.PublisherFlush;
import jp.go.aist.rtm.RTC.util.CORBA_SeqUtil;
import jp.go.aist.rtm.RTC.util.NVUtil;
import jp.go.aist.rtm.RTC.util.DataRef;
import jp.go.aist.rtm.RTC.util.ORBUtil;
import jp.go.aist.rtm.RTC.util.NVListHolderFactory;
import jp.go.aist.rtm.RTC.util.Properties;

import RTC.ConnectorProfileHolder;

public class InPortCorbaCdrConsumerTest extends TestCase {
    /*!
     * 
     * 
     */
    class InPortCorbaCdrConsumerMock extends InPortCorbaCdrConsumer {
        /**
         * 
         * 
         */
        public InPortCorbaCdrConsumerMock() {
            super();
        }
        /**
         * for check
         */
        org.omg.CORBA.Object get_m_objre() {
            return m_objref;
        }
    };

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
    }
    /**
     * <p> Test finalization </p>
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    /**
     * <p>  </p>
     * 
     */
    public void test_case0() {
        //
        //
        //
        InPortCorbaCdrConsumerMock consumer = new InPortCorbaCdrConsumerMock();

        RTC.ConnectorProfile prof = new RTC.ConnectorProfile();
        boolean ret;
        byte[] testdata = { 12,34,56,78,90,23,45, };

        PublisherFlush.PublisherFlushInit();
        CdrRingBuffer.CdrRingBufferInit();
        {
        NVListHolder holder = new NVListHolder(prof.properties);
        ret = consumer.subscribeInterface(holder);
        prof.properties = holder.value;
        }
        //subscribeInterface() returns false
        // because it has called subscribeInterface() before setting IOR.
        assertEquals(false, ret);

        InPortCorbaCdrProvider provider = new InPortCorbaCdrProvider();

        {
        NVListHolder holder = new NVListHolder(prof.properties);
        CORBA_SeqUtil.push_back(holder,
                                NVUtil.newNV("dataport.interface_type",
                                               "corba_cdr"));
        prof.properties = holder.value;
        }
        {
        NVListHolder holder = new NVListHolder(prof.properties);
        provider.publishInterface(holder);
        prof.properties = holder.value;
        }


        {
        NVListHolder holder = new NVListHolder(prof.properties);
        ret = consumer.subscribeInterface(holder);
        prof.properties = holder.value;
        assertEquals(true, ret);
        }
        {
        Properties prop = new Properties();
        ConnectorBase.ConnectorInfo profile
            = new ConnectorBase.ConnectorInfo(prof.name,
                                  prof.connector_id,
                                  new Vector<String>(),
                                  prop);
        ConnectorListeners listeners = new ConnectorListeners();
        try {
            OutPortConnector out_connector = new OutPortPushConnector(profile,
                                                listeners,
                                                consumer);
            consumer.setConnector(out_connector);
            InPortConnector in_connector = new InPortPushConnector(profile,
                                                provider,
                                                listeners,
                                                null);
            provider.setConnector(in_connector);
        }
        catch (Exception e) {
        }
        }
        java.util.Properties props = new java.util.Properties();
        props.put("org.omg.CORBA.ORBInitialPort", "2809");
        props.put("org.omg.CORBA.ORBInitialHost", "localhost");
        ORB orb = ORB.init(new String[0], props);
        OutputStream indata
            = new EncapsOutputStreamExt(orb,true);

        ReturnCode retcode;

        RTC.Time tm = new RTC.Time(123,127);
        RTC.TimedOctetSeq tmoct = new RTC.TimedOctetSeq(tm,testdata);
        RTC.TimedOctetSeqHolder tmoctholder 
            = new RTC.TimedOctetSeqHolder(tmoct);
        tmoctholder._write(indata);
         

        //put() is called before the buffer is set to the provider.
        //If there is no buffer setting, set the default.
        retcode = consumer.put(indata);
        assertEquals(ReturnCode.PORT_OK, retcode);

        RingBuffer<OutputStream> buffer;
        final BufferFactory<RingBuffer<OutputStream>,String> factory 
            = BufferFactory.instance();
        factory.addFactory("ring_buffer",
                    new CdrRingBuffer(),
                    new CdrRingBuffer());
        buffer = factory.createObject("ring_buffer");
        {
        Properties prop = new Properties();
        prop.setProperty("write.full_policy","do_nothing");
        buffer.init(prop);
        }
        provider.setBuffer(buffer);

        for(int ic=0;ic<8;++ic) {
            retcode = consumer.put(indata);
            assertEquals(ReturnCode.PORT_OK, retcode);
         }

        //The buffer is full, and put() is called. 
        retcode = consumer.put(indata);
        assertEquals(ReturnCode.BUFFER_FULL, retcode);


        for(int icc=0;icc<8;++icc) {
            OutputStream cdr
                = new EncapsOutputStreamExt(orb,true);
            DataRef<OutputStream> ref = new DataRef<OutputStream>(cdr);
            buffer.read(ref);
            RTC.TimedOctetSeq to = new RTC.TimedOctetSeq();
            RTC.TimedOctetSeqHolder toh 
                = new RTC.TimedOctetSeqHolder(to);
            toh._read(ref.v.create_input_stream());

            assertEquals(123,toh.value.tm.sec); 
            assertEquals(127,toh.value.tm.nsec); 
            for(int ic=0;ic<testdata.length;++ic) {
                assertEquals(testdata[ic],toh.value.data[ic]);
            }
        }
        assertTrue(consumer.get_m_objre()!=null);
        {
        NVListHolder holder = new NVListHolder(prof.properties);
        consumer.unsubscribeInterface(holder);
        prof.properties = holder.value;
        }
        assertTrue(consumer.get_m_objre()==null);

        int index;
        {
        NVListHolder holder = new NVListHolder(prof.properties);
        index = NVUtil.find_index(holder,
                                   "dataport.corba_cdr.inport_ior");
        prof.properties = holder.value;
        }

         String ior = new String();
         try {
             ior = prof.properties[index].value.extract_wstring();
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
    
    /**
     * <p>  </p>
     * 
     */
    public void test_timedlong() {
        //
        //
        //
        InPortCorbaCdrConsumerMock consumer = new InPortCorbaCdrConsumerMock();

        RTC.ConnectorProfile prof = new RTC.ConnectorProfile();
        boolean ret;
        int[] testdata = { 12345,67890,123456,789012,4,8,15,16,23,42 };

        PublisherFlush.PublisherFlushInit();
        CdrRingBuffer.CdrRingBufferInit();

        {
        NVListHolder holder = new NVListHolder(prof.properties);
        ret = consumer.subscribeInterface(holder);
        prof.properties = holder.value;
        }
        //subscribeInterface() returns false
        // because it has called subscribeInterface() before setting IOR.
        assertEquals(false, ret);

        InPortCorbaCdrProvider provider = new InPortCorbaCdrProvider();

        {
        NVListHolder holder = new NVListHolder(prof.properties);
        CORBA_SeqUtil.push_back(holder,
                                NVUtil.newNV("dataport.interface_type",
                                               "corba_cdr"));
        CORBA_SeqUtil.push_back(holder,
                                NVUtil.newNV("dataport.buffer_type",
                                               "ring_buffer"));
        prof.properties = holder.value;
        }
        {
        NVListHolder holder = new NVListHolder(prof.properties);
        provider.publishInterface(holder);
        prof.properties = holder.value;
        }


        {
        NVListHolder holder = new NVListHolder(prof.properties);
        ret = consumer.subscribeInterface(holder);
        prof.properties = holder.value;
        assertEquals(true, ret);
        }

        {
        Properties prop = new Properties();
        ConnectorBase.ConnectorInfo profile
            = new ConnectorBase.ConnectorInfo(prof.name,
                                  prof.connector_id,
                                  new Vector<String>(),
                                  prop);
        ConnectorListeners listeners = new ConnectorListeners();
        try {
            OutPortConnector out_connector = new OutPortPushConnector(profile,
                                                listeners,
                                                consumer);
            consumer.setConnector(out_connector);
            InPortConnector in_connector = new InPortPushConnector(profile,
                                                provider,
                                                listeners,
                                                null);
            provider.setConnector(in_connector);
        }
        catch (Exception e) {
        }
        }
        java.util.Properties props = new java.util.Properties();
        props.put("org.omg.CORBA.ORBInitialPort", "2809");
        props.put("org.omg.CORBA.ORBInitialHost", "localhost");
        ORB orb = ORB.init(new String[0], props);
        OutputStream indata
            = new EncapsOutputStreamExt(orb,true);
        ReturnCode retcode;

        RTC.Time tm = new RTC.Time(123,127);
        RTC.TimedLongSeq tmlong = new RTC.TimedLongSeq(tm,testdata);
        RTC.TimedLongSeqHolder tmlongholder 
            = new RTC.TimedLongSeqHolder(tmlong);
        tmlongholder._write(indata);
        

        RingBuffer<OutputStream> buffer;
        final BufferFactory<RingBuffer<OutputStream>,String> factory 
            = BufferFactory.instance();
        factory.addFactory("ring_buffer",
                    new CdrRingBuffer(),
                    new CdrRingBuffer());
        buffer = factory.createObject("ring_buffer");
        provider.setBuffer(buffer);

        for(int ic=0;ic<8;++ic) {
            retcode = consumer.put(indata);
            assertEquals(ReturnCode.PORT_OK, retcode);
        }


        for(int icc=0;icc<8;++icc) {
            OutputStream cdr
                = new EncapsOutputStreamExt(orb,true);
            DataRef<OutputStream> ref = new DataRef<OutputStream>(cdr);
            buffer.read(ref);
            RTC.TimedLongSeq tl = new RTC.TimedLongSeq();
            RTC.TimedLongSeqHolder tlh 
                = new RTC.TimedLongSeqHolder(tl);
            tlh._read(ref.v.create_input_stream());

            assertEquals(123,tlh.value.tm.sec); 
            assertEquals(127,tlh.value.tm.nsec); 
            for(int ic=0;ic<testdata.length;++ic) {
                assertEquals(testdata[ic],tlh.value.data[ic]);
            }
        }
        assertTrue(consumer.get_m_objre()!=null);
        {
        NVListHolder holder = new NVListHolder(prof.properties);
        consumer.unsubscribeInterface(holder);
        prof.properties = holder.value;
        }
        assertTrue(consumer.get_m_objre()==null);

        int index;
        {
        NVListHolder holder = new NVListHolder(prof.properties);
        index = NVUtil.find_index(holder,
                                   "dataport.corba_cdr.inport_ior");
        prof.properties = holder.value;
        }

        String ior = new String();
        try {
             ior = prof.properties[index].value.extract_wstring();
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

