package jp.go.aist.rtm.RTC;

import junit.framework.TestCase;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;

import jp.go.aist.rtm.RTC.util.Properties;
import jp.go.aist.rtm.RTC.sample.SampleComponentDelete;
import jp.go.aist.rtm.RTC.sample.SampleComponentNew;
/**
* NumberingPolicy　テスト
* 対象クラス：NumberingPolicy
*/
public class NumberingPolicyTests extends TestCase {
    
    private ORB m_pORB;
    private POA m_pPOA;
    

    protected void setUp() throws Exception {
        super.setUp();

        // (1-1) ORBの初期化
        java.util.Properties props = new java.util.Properties();
        props.put("org.omg.CORBA.ORBInitialPort", "2809");
        props.put("org.omg.CORBA.ORBInitialHost", "localhost");
        this.m_pORB = ORB.init(new String[0], props);

        // (1-2) POAManagerのactivate
        this.m_pPOA = org.omg.PortableServer.POAHelper.narrow(
                this.m_pORB.resolve_initial_references("RootPOA"));
        this.m_pPOA.the_POAManager().activate();

    }

    protected void tearDown() throws Exception {
        super.tearDown();
        this.m_pORB.destroy();
    }
    private RTObject_impl prepareRTObject() {
        Manager manager = Manager.instance();
        manager.activateManager();
     
        String component_conf[] = {
                "implementation_id", "sample",
                "type_name",         "type_sample",
                "description",       "description_sample",
                "version",           "version_sample",
                "vendor",            "vendor_sample",
                "category",          "category_sample",
                "activity_type",     "activity_type_sample",
                "max_instance",      "max_instance_sample",
                "language",          "language_sample",
                "lang_type",         "",
                "conf",              "",
                ""
        };
        Properties prop = new Properties(component_conf);
        manager.registerFactory(prop, new SampleComponentNew(), new SampleComponentDelete());
        RTObject_impl rtobj = manager.createComponent("sample");
        return rtobj;
    }

    /**
     * <p>DefaultNumberingPolicy::onCreate()とDefaultNumberingPolicy::onDelete()のテスト
     * <ul>
     * <li>onCreate()は意図どおりに名称を生成して返すか？</li>
     * <li>onDelete()で正しく登録解除されるか？</li>
     * <li>登録解除後に、onCreate()で登録した場合、解除されたオブジェクトの番号が再利用されるか？</li>
     * </ul>
     * </p>
     */
     public void test_onCreate_and_onDelete() throws Exception {
         DefaultNumberingPolicy policy = new DefaultNumberingPolicy();
         RTObject_impl rto1 = prepareRTObject();
	 RTObject_impl rto2 = prepareRTObject();
	 RTObject_impl rto3 = prepareRTObject();
            
         // onCreate()は意図どおりに名称を生成して返すか？
         assertEquals("0", policy.onCreate(rto1));
         assertEquals("1", policy.onCreate(rto2));
         assertEquals("2", policy.onCreate(rto3));
         
         // onDeleteで、いったん登録解除する
         policy.onDelete(rto1);
         policy.onDelete(rto2);
            
         // 登録順を入れ換えて再度onCreateを呼び出した場合、意図どおりの名称がアサインされるか？
         // （登録解除後に、onCreate()で登録した場合、解除されたオブジェクトの番号が再利用されるか？）
         assertEquals("0", policy.onCreate(rto2));
         assertEquals("1", policy.onCreate(rto1));

      }
}
