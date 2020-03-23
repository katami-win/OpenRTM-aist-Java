package jp.go.aist.rtm.RTC.port;

import junit.framework.TestCase;

import org.omg.CORBA.ORB;
import org.omg.CORBA.portable.OutputStream;

import RTC.Time;
import RTC.TimedLong;
import RTC.TimedLongHolder;

import jp.go.aist.rtm.RTC.SerializerFactory;
import jp.go.aist.rtm.RTC.util.DataRef;

public class CORBA_CdrSerializeTests extends TestCase {
    protected void setUp() throws Exception {
        super.setUp();
        CORBA_CdrSerializer.CORBA_CdrSerializerInit();
        final SerializerFactory<CORBA_CdrSerializer,String> factory 
            = SerializerFactory.instance();
        m_serializer = factory.createObject("corba");
    }
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    public void test_serialize(){
        TimedLong data = new TimedLong(new Time(0,0),12345);
	java.util.Properties props = new java.util.Properties();
        props.put("org.omg.CORBA.ORBInitialPort", "2809");
        props.put("org.omg.CORBA.ORBInitialHost", "localhost");
        ORB orb = ORB.init(new String[0], props);

        OutputStream cdr 
            = new EncapsOutputStreamExt(orb,true);
        SerializeReturnCode ser_ret;
        ser_ret = m_serializer.serialize(data,cdr);
        assertEquals(ser_ret, SerializeReturnCode.SERIALIZE_OK);

        TimedLong data2 = new TimedLong(new Time(0,0),0);
        DataRef<TimedLong> dataref = new DataRef<TimedLong>(data2);
        ser_ret = m_serializer.deserialize(dataref,cdr);
        assertEquals(ser_ret, SerializeReturnCode.SERIALIZE_OK);
        assertEquals(12345, dataref.v.data);
    }
    private CORBA_CdrSerializer m_serializer;
}


