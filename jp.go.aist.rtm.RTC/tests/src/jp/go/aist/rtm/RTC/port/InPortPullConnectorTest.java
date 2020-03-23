package jp.go.aist.rtm.RTC.port;

import junit.framework.TestCase;

import java.util.Vector;

import org.omg.CORBA.ORB;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;

import _SDOPackage.NVListHolder;
import RTC.PortService;
import RTC.Time;
import RTC.TimedLong;
import RTC.TimedLongHolder;

import jp.go.aist.rtm.RTC.BufferFactory;
import jp.go.aist.rtm.RTC.buffer.BufferBase;
import jp.go.aist.rtm.RTC.buffer.CdrRingBuffer;
import jp.go.aist.rtm.RTC.buffer.RingBuffer;
import jp.go.aist.rtm.RTC.util.CORBA_SeqUtil;
import jp.go.aist.rtm.RTC.util.DataRef;
import jp.go.aist.rtm.RTC.util.Properties;
import jp.go.aist.rtm.RTC.util.NVUtil;

public class InPortPullConnectorTest extends TestCase {
    /**
     * 
     * 
     *
     */
    class OutPortCorbaCdrConsumerMock extends OutPortCorbaCdrConsumer {

        public OutPortCorbaCdrConsumerMock() {
        }
        /**
         *
         *
         */
        public void init(Properties prop) {
          // ここで指定したバッファが使用されることを確認する
          //prop.setProperty("buffer_type", "ring_buffer_mock2");
        }
        /**
         *
         *
         */
        public ReturnCode get(OutputStream data) {
            TimedLong tmlong = new TimedLong(new Time(0,0),12345);
            TimedLongHolder holder = new TimedLongHolder(tmlong);
            holder._write(data);
            return ReturnCode.PORT_OK;
        }
        /**
         *
         *
         */
        public boolean subscribeInterface(final _SDOPackage.NVListHolder properties) {
            return true;
        }
  
        /**
         *
         *
         */
        public void unsubscribeInterface(final _SDOPackage.NVListHolder properties) {
        }
    }
    /**
     * <p> Test initialization </p>
     */
    protected void setUp() throws Exception {
    
        super.setUp();
        CdrRingBufferMock3.CdrRingBufferMockInit();
        CORBA_CdrSerializer.CORBA_CdrSerializerInit();
    }
		
    /**
     * <p> Test finalization </p>
     */
    protected void tearDown() throws Exception {
    }
    /**
     *  <p> read </p>
     * 
     */
    public void test_read()   throws Exception {
        RingBuffer<OutputStream> pbuffer = new RingBuffer<OutputStream>();
        RTC.ConnectorProfile prof = new RTC.ConnectorProfile();
        NVListHolder prof_holder = new NVListHolder(prof.properties);
        CORBA_SeqUtil.push_back(prof_holder,
	  		       NVUtil.newNV("dataport.buffer_type",
					     "ring_buffer_mock"));
        prof.properties = prof_holder.value;
        prof.ports = new PortService[2];
        // prop: [port.outport].
        Properties prop = new Properties();
        {
            Properties conn_prop = new Properties();
            NVListHolder holder = new NVListHolder(prof.properties);
            NVUtil.copyToProperties(conn_prop, holder);
            prop.merge(conn_prop.getNode("dataport"));
        }
        OutPortCorbaCdrConsumerMock consumer = new OutPortCorbaCdrConsumerMock();
        ConnectorBase.ConnectorInfo profile_new = new ConnectorBase.ConnectorInfo (
                                   prof.name,
                                   prof.connector_id,
                                   CORBA_SeqUtil.refToVstring(prof.ports),
                                   prop); 
        InPortConnector connector= null;
        ConnectorListeners listeners = new ConnectorListeners();
        connector = new InPortPullConnector(profile_new, consumer, listeners, pbuffer);

        assertNotNull("connector is null.",connector);

        java.util.Properties uprops = new java.util.Properties();
        uprops.put("org.omg.CORBA.ORBInitialPort", "2809");
        uprops.put("org.omg.CORBA.ORBInitialHost", "localhost");
        ORB orb = ORB.init(new String[0], uprops);
        OutputStream ostream
                = new EncapsOutputStreamExt(orb,true);

        InputStream cdr = ostream.create_input_stream();
        DataRef<InputStream> cdrref = new DataRef<InputStream>(cdr);
        ReturnCode read_ret = connector.read(cdrref);
        assertEquals(read_ret,ReturnCode.PORT_OK);

        TimedLong tmlong = new TimedLong(new Time(0,0),0);
        TimedLongHolder holder = new TimedLongHolder(tmlong);
        holder._read(cdrref.v);
        assertEquals(holder.value.data,12345);
    }
}
    /**
     * 
     * 
     *
     */
    class CdrRingBufferMock3 extends CdrRingBuffer{
    
        /**
         * <p> creator_ </p>
         * 
         * @return Object Created instances
         *
         */
        public BufferBase<OutputStream> creator_() {
            return new RingBufferMock<OutputStream>();
        }
        /**
         * <p> destructor_ </p>
         * 
         * @param obj    The target instances for destruction
         *
         */
        public void destructor_(Object obj) {
            obj = null;
        }

        /**
         * <p> CdrRingBufferInit </p>
         *
         */
        public static void CdrRingBufferMockInit() {
            final BufferFactory<RingBufferMock<OutputStream>,String> factory 
                = BufferFactory.instance();

            factory.addFactory("ring_buffer_mock",
                        new CdrRingBufferMock(),
                        new CdrRingBufferMock());
    
        }
    }
    /**
     * 
     * 
     *
     */
    class RingBufferMock3<DataType> extends RingBuffer<DataType>{
        public RingBufferMock3() {
        }
        /**
         *
         *
         */
        public  void init(final Properties prop) {
        }
        /**
         *
         *
         */
        public int length() {
            return 0;
        }
        /**
         *
         *
         */
        public jp.go.aist.rtm.RTC.buffer.ReturnCode length(int n) {
            return jp.go.aist.rtm.RTC.buffer.ReturnCode.BUFFER_OK; //BUFFER_OK;
        }
        /**
         *
         *
         */
        public jp.go.aist.rtm.RTC.buffer.ReturnCode reset() {
            return jp.go.aist.rtm.RTC.buffer.ReturnCode.BUFFER_OK; //BUFFER_OK;
        }
        /**
         *
         *
         */
        public DataType wptr(int n) {
            return m_data;
        }
        /**
         *
         *
         */
        public  jp.go.aist.rtm.RTC.buffer.ReturnCode advanceWptr(int n) {
            return jp.go.aist.rtm.RTC.buffer.ReturnCode.BUFFER_OK; //BUFFER_OK;
        }
        /**
         *
         *
         */
        public jp.go.aist.rtm.RTC.buffer.ReturnCode put(final DataType value) {
            return jp.go.aist.rtm.RTC.buffer.ReturnCode.BUFFER_OK; //BUFFER_OK;
        }
        /**
         *
         *
         */
        public  int writable() {
            return 0;
        }
        /**
         *
         *
         */
        public boolean full() {
              return true;
        }
        /**
         *
         *
         */
        public DataType rptr(int n ) {
            return m_data;
        }
        /**
         *
         *
         */
        public jp.go.aist.rtm.RTC.buffer.ReturnCode advanceRptr(int n) {
            return jp.go.aist.rtm.RTC.buffer.ReturnCode.BUFFER_OK; //BUFFER_OK;
        }
        /**
         *
         *
         */
        public jp.go.aist.rtm.RTC.buffer.ReturnCode get(DataType value) {
            return jp.go.aist.rtm.RTC.buffer.ReturnCode.BUFFER_OK; //BUFFER_OK;
        }
        /**
         *
         *
         */
        public DataType  get() {
            return m_data;
        }
        /**
         *
         *
         */
        public int readable() {
            return 0;
        }
        /**
         *
         *
         */
        public boolean empty() {
            return true;
        }
        private DataType m_data;
        private Vector<DataType> m_buffer;
  };

