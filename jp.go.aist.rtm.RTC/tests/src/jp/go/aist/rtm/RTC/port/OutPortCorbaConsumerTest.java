package jp.go.aist.rtm.RTC.port;

import jp.go.aist.rtm.RTC.ObjectCreator;
import jp.go.aist.rtm.RTC.ObjectDestructor;
import jp.go.aist.rtm.RTC.buffer.RingBuffer;
import jp.go.aist.rtm.RTC.buffer.ReturnCode;
import jp.go.aist.rtm.RTC.buffer.BufferBase;
import jp.go.aist.rtm.RTC.util.CORBA_SeqUtil;
import jp.go.aist.rtm.RTC.util.DataRef;
import jp.go.aist.rtm.RTC.util.NVListHolderFactory;
import jp.go.aist.rtm.RTC.util.NVUtil;
import jp.go.aist.rtm.RTC.util.ORBUtil;
import jp.go.aist.rtm.RTC.util.FloatHolder;
import jp.go.aist.rtm.RTC.util.Properties;
import junit.framework.TestCase;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.PortableServer.POA;

//import RTC.OutPortAnyPOA;
import OpenRTM.OutPortCdrPOA;

import _SDOPackage.NVListHolder;

import com.sun.corba.se.impl.encoding.EncapsOutputStream; 


/**
 * <p>OutPortCorbaConsumerクラスのためのテストケースです。</p>
 */
public class OutPortCorbaConsumerTest extends TestCase {

    class OutPortAnyMock extends OutPortCdrPOA {
        private OutputStream m_data;

        public void setData(OutputStream data) {
            m_data = data;
        }
        public OutputStream get() {
            return m_data;
        }
        public OpenRTM.PortStatus get(OpenRTM.CdrDataHolder data) {
            OutputStream cdr = null;
            DataRef<OutputStream> cdr_ref = new DataRef<OutputStream>(cdr);
            EncapsOutputStreamExt outcdr;
            outcdr = (EncapsOutputStreamExt)m_data;
            data.value =  outcdr.getByteArray();


            return OpenRTM.PortStatus.PORT_OK;
        }
    }

    private ORB m_orb;
    private POA m_poa;

    protected void setUp() throws Exception {
        super.setUp();
        // (1-1) ORBの初期化
        java.util.Properties props = new java.util.Properties();
        props.put("org.omg.CORBA.ORBInitialPort", "2809");
        props.put("org.omg.CORBA.ORBInitialHost", "localhost");
        this.m_orb = ORB.init(new String[0], props);

        // (1-2) POAManagerのactivate
        this.m_poa = org.omg.PortableServer.POAHelper.narrow(
                this.m_orb.resolve_initial_references("RootPOA"));
        this.m_poa.the_POAManager().activate();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * <p>get()メソッドのテスト
     * <ul>
     * <li>OutPortConsumerのget()メソッド呼出によって、Provider側のデータを正しく取得できる？</li>
     * </ul>
     * </p>
     */
    public void test_get() throws Exception {
        OutPortAnyMock outPortAny = new OutPortAnyMock();
        byte[] oid = this.m_poa.activate_object(outPortAny);

        RingBuffer<Float> buffer = new RingBuffer<Float>(100);
        OutPortCorbaCdrConsumer consumer = new OutPortCorbaCdrConsumer();
        consumer.setObject(m_poa.id_to_reference(oid));
        
        // InPortConsumerのput()メソッドを呼び出す
        java.util.Properties uprops = new java.util.Properties();
        uprops.put("org.omg.CORBA.ORBInitialPort", "2809");
        uprops.put("org.omg.CORBA.ORBInitialHost", "localhost");
        ORB orb = ORB.init(new String[0], uprops);
        OutputStream ostream
                = new EncapsOutputStreamExt(orb,true);
        RTC.Time tm = new RTC.Time(0,0);
        RTC.TimedFloat tmlong = new RTC.TimedFloat(tm,3.14159f);
        RTC.TimedFloatHolder tmlongholder
                = new RTC.TimedFloatHolder(tmlong);
        tmlongholder._write(ostream);
        outPortAny.setData(ostream);
        
        // OutPortCorbaConsumer::get()を用いて、データを読み取る
        EncapsOutputStream cdr
                = new EncapsOutputStreamExt(orb,true);
        consumer.get(cdr);
        InputStream data = cdr.create_input_stream();
        RTC.TimedFloatHolder holder = new RTC.TimedFloatHolder();
        holder._read(data);
        DataRef<Float> readValue = new DataRef<Float>(0f);
        readValue.v = holder.value.data;
        
        // テスト用に設定しておいたデータを読み取ったデータを比較し、正しく取得できたことを確認する
        assertTrue( Math.abs(tmlongholder.value.data-readValue.v) < 0.00001);
    }
    /**
     * <p>subscribeInterface()メソッドのテスト
     * <ul>
     * <li>プロパティにOutPortAnyのリファレンスを設定して、subscribeInterface()により登録が成功するか？</li>
     * </ul>
     * </p>
     */
    public void test_subscribeInterface() throws Exception {
        OutPortAnyMock outPortAny = new OutPortAnyMock();
        byte[] oid = this.m_poa.activate_object(outPortAny);

        RingBuffer<Float> buffer = new RingBuffer<Float>(100);
        OutPortCorbaCdrConsumer consumer = new OutPortCorbaCdrConsumer();
        org.omg.CORBA.Object outPortAnyRef = m_poa.id_to_reference(oid);

        // プロパティにOutPortAnyのリファレンスを設定して、subscribeInterface()により登録が成功するか？
        NVListHolder properties = NVListHolderFactory.create();
        CORBA_SeqUtil.push_back(properties, NVUtil.newNV("dataport.dataflow_type", "Pull"));
        CORBA_SeqUtil.push_back(properties, NVUtil.newNV("dataport.corba_any.outport_ref", outPortAnyRef, Object.class));

        java.util.Properties props = new java.util.Properties();
        props.put("org.omg.CORBA.ORBInitialPort", "2809");
        props.put("org.omg.CORBA.ORBInitialHost", "localhost");
        ORB orb = ORB.init(new String[0], props);
        String iorstr = orb.object_to_string(outPortAnyRef);
        CORBA_SeqUtil.push_back(properties, NVUtil.newNV("dataport.corba_cdr.outport_ior", iorstr));

        assertTrue(consumer.subscribeInterface(properties));
    }
}
