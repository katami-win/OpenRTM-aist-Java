package jp.go.aist.rtm.RTC.port;

import jp.go.aist.rtm.RTC.BufferFactory;
import jp.go.aist.rtm.RTC.buffer.RingBuffer;
import jp.go.aist.rtm.RTC.buffer.CdrRingBuffer;
import jp.go.aist.rtm.RTC.util.NVListHolderFactory;
import jp.go.aist.rtm.RTC.util.NVUtil;
import jp.go.aist.rtm.RTC.util.FloatHolder;
import jp.go.aist.rtm.RTC.util.DataRef;
import jp.go.aist.rtm.RTC.util.ORBUtil;
import jp.go.aist.rtm.RTC.util.Properties;
import junit.framework.TestCase;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;

import RTC.TimedFloat;
import _SDOPackage.NVListHolder;

import com.sun.corba.se.impl.encoding.EncapsOutputStream; 
import com.sun.corba.se.impl.encoding.EncapsInputStream; 

/**
 * <p>OutPortProviderImplクラスのためのテストケースです。</p>
 */
public class OutPortCorbaProviderTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * <p>publishInterfaceProfile()メソッドのテスト
     * <ul>
     * <li>"dataport.data_type"プロパティを正しく取得できるか？</li>
     * <li>"dataport.interface_type"プロパティを正しく取得できるか？</li>
     * <li>"dataport.dataflow_type"プロパティを正しく取得できるか？</li>
     * <li>"dataport.subscription_type"プロパティを正しく取得できるか？</li>
     * </ul>
     * </p>
     */
    public void test_publishInterfaceProfile() throws Exception {
        RingBuffer<TimedFloat> buffer = new RingBuffer<TimedFloat>(100);
        OutPortCorbaCdrProvider provider = new OutPortCorbaCdrProvider(); // will be deleted automatically
        
        NVListHolder profile = NVListHolderFactory.create();

        provider.publishInterfaceProfile(profile);
        
        
        // "dataport.interface_type"プロパティを正しく取得できるか？
        assertEquals("corba_cdr", NVUtil.toString(profile, "dataport.interface_type"));
        
        
    }
    /**
     * <p>get()メソッドのテスト
     * <ul>
     * <li>バッファに書き込まれた値を、get()メソッドで正しく読み出せるか？</li>
     * </ul>
     * </p>
     */
    public void test_get() throws Exception {
        OutPortCorbaCdrProvider provider = new OutPortCorbaCdrProvider(); // will be deleted automatically

        RingBuffer<OutputStream> buffer;
        final BufferFactory<RingBuffer<OutputStream>,String> factory
            = BufferFactory.instance();
        factory.addFactory("ring_buffer",
                    new CdrRingBuffer(),
                    new CdrRingBuffer());
        buffer = factory.createObject("ring_buffer");
        Properties prop = new Properties();
        prop.setProperty("read.empty_policy","do_nothing");
        buffer.init(prop);
        provider.setBuffer(buffer);

        for( int i = 0; i < 10; ++i ) {
            java.util.Properties props = new java.util.Properties();
            props.put("org.omg.CORBA.ORBInitialPort", "2809");
            props.put("org.omg.CORBA.ORBInitialHost", "localhost");
            ORB orb = ORB.init(new String[0], props);
            OutputStream cdr
                = new EncapsOutputStreamExt(orb,true);
            RTC.TimedFloat tmfloat = new RTC.TimedFloat(new RTC.Time(0,0),3.14159f * i);
            RTC.TimedFloatHolder tmfloatholder
                = new RTC.TimedFloatHolder(tmfloat);
            tmfloatholder._write(cdr);
            buffer.write(cdr);

            // バッファに書き込まれた値を、get()メソッドで正しく読み出せるか？

            byte[] byte_data = new byte[256];
            OpenRTM.CdrDataHolder cdr_data = new OpenRTM.CdrDataHolder(byte_data);
            provider.get(cdr_data);
            OutputStream data
                = new EncapsOutputStreamExt(orb,true);
            data.write_octet_array(cdr_data.value, 0, 
                                        cdr_data.value.length);
            EncapsOutputStream outcdr;
            outcdr = (EncapsOutputStream)data;
            InputStream dataref
                    = outcdr.create_input_stream();
            RTC.TimedFloat fl = new RTC.TimedFloat(new RTC.Time(0,0),0);
            RTC.TimedFloatHolder holder = new RTC.TimedFloatHolder(fl);
            holder._read(dataref);

            assertEquals(tmfloat.data, holder.value.data);
        }
    }
    
}
