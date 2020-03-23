package jp.go.aist.rtm.RTC.port;

import junit.framework.TestCase;

import java.util.Vector;

import org.omg.CORBA.ORB;
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
import jp.go.aist.rtm.RTC.util.ORBUtil;
import jp.go.aist.rtm.RTC.util.Properties;
import jp.go.aist.rtm.RTC.util.NVUtil;

public class OutPortPullConnectorTest extends TestCase {
    /**
     * <p> Test initialization </p>
     */
    protected void setUp() throws Exception {
    
        super.setUp();
        CORBA_CdrSerializer.CORBA_CdrSerializerInit();
    }
		
    /**
     * <p> Test finalization </p>
     */
    protected void tearDown() throws Exception {
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
     *  <p> read </p>
     * 
     */
    public void test_write()   throws Exception {
        RTC.ConnectorProfile prof = new RTC.ConnectorProfile();
        NVListHolder prof_holder = new NVListHolder(prof.properties);
        CORBA_SeqUtil.push_back(prof_holder,
  			       NVUtil.newNV("dataport.interface_type",
  					     "corba_cdr"));
        prof.properties = prof_holder.value;
        CORBA_SeqUtil.push_back(prof_holder,
			       NVUtil.newNV("dataport.dataflow_type",
  			     "push"));
        prof.properties = prof_holder.value;
        CORBA_SeqUtil.push_back(prof_holder,
  	  		       NVUtil.newNV("dataport.subscription_type",
  					     "new"));
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
        OutPortCorbaCdrProvider provider = new OutPortCorbaCdrProvider();
        ConnectorBase.ConnectorInfo profile_new = new ConnectorBase.ConnectorInfo(
                                   prof.name,
                                   prof.connector_id,
                                   CORBA_SeqUtil.refToVstring(prof.ports),
                                   prop); 
        RingBufferMock4<OutputStream> buffer = new RingBufferMock4<OutputStream>();
        OutPortConnector connector = null;
        ConnectorListeners listeners = new ConnectorListeners();
        connector = new OutPortPullConnector(profile_new, provider, listeners, buffer);
        OutputStream cdr = toStream(12345,0,0);
        RTC.TimedLong out_val = new RTC.TimedLong(new RTC.Time(0,0),12345);
        DataRef<RTC.TimedLong> out = new DataRef<RTC.TimedLong>(out_val);
        OutPort<RTC.TimedLong> outPort = new OutPort<RTC.TimedLong>("out", out);
        connector.setOutPortBase(outPort);
        ReturnCode ret = connector.write(out.v);
        assertEquals(ret,ReturnCode.PORT_OK);
	
        OutputStream retdata = buffer.get();
        TimedLong tmlong = new TimedLong(new Time(0,0),12345);
        TimedLongHolder holder = new TimedLongHolder(tmlong);
        holder._write(retdata);
        assertEquals(holder.value.data,12345);
    }

}
    /**
     * 
     * 
     *
     */
    class RingBufferMock4<DataType> extends RingBuffer<DataType>{
        public RingBufferMock4() {
            m_read_return_value = jp.go.aist.rtm.RTC.buffer.ReturnCode.BUFFER_OK;
            m_write_return_value = jp.go.aist.rtm.RTC.buffer.ReturnCode.BUFFER_OK;
        }
        /**
         *
         *
         */
        public void set_read_return_value(jp.go.aist.rtm.RTC.buffer.ReturnCode value) {
            m_read_return_value = value;
        }
        /**
         *
         *
         */
        public void set_write_return_value(jp.go.aist.rtm.RTC.buffer.ReturnCode value) {
            m_write_return_value = value;
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
        public jp.go.aist.rtm.RTC.buffer.ReturnCode write(final DataType value,
                                 int sec, int nsec) {
            m_data = value;
            return jp.go.aist.rtm.RTC.buffer.ReturnCode.BUFFER_OK;
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
        public jp.go.aist.rtm.RTC.buffer.ReturnCode read(DataRef<DataType> value,
                              int sec, int nsec) {
            return m_read_return_value; //BUFFER_OK;
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
        private jp.go.aist.rtm.RTC.buffer.ReturnCode m_read_return_value;
        private jp.go.aist.rtm.RTC.buffer.ReturnCode m_write_return_value;
  };

