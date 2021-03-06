package jp.go.aist.rtm.RTC;

import jp.go.aist.rtm.RTC.SDOPackage.Configuration_impl;
import jp.go.aist.rtm.RTC.util.FloatHolder;
import jp.go.aist.rtm.RTC.util.IntegerHolder;
import jp.go.aist.rtm.RTC.util.ORBUtil;
import jp.go.aist.rtm.RTC.util.Properties;
import jp.go.aist.rtm.RTC.util.ShortHolder;
import junit.framework.TestCase;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TCKind;
import org.omg.PortableServer.POA;

import _SDOPackage.Configuration;
import _SDOPackage.ConfigurationSet;
import _SDOPackage.ConfigurationSetListHolder;
import _SDOPackage.DeviceProfile;
import _SDOPackage.InternalError;
import _SDOPackage.InvalidParameter;
import _SDOPackage.NVListHolder;
import _SDOPackage.NameValue;
import _SDOPackage.NotAvailable;
import _SDOPackage.Organization;
import _SDOPackage.OrganizationListHolder;
import _SDOPackage.ParameterListHolder;
import _SDOPackage.SDOService;
import _SDOPackage.SDOServicePOA;
import _SDOPackage.ServiceProfile;
import _SDOPackage.ServiceProfileListHolder;
import _SDOPackage._OrganizationStub;

/**
*
* SDOコンフィギュレーションセットクラス　テスト
* 対象クラス：Configuration_impl
*
*/
public class SdoConfigurationTest extends TestCase {
    private Configuration_impl m_pConf;
    private ORB m_orb;
    private POA m_poa;
    private SdoServiceAdmin m_sdoservice;
    
    public SdoConfigurationTest() {
    }
    class ComponentObserverMock extends OpenRTM.ComponentObserverPOA{
        public void update_status (OpenRTM.StatusKind status_kind, String hint){
        }
    }
    class SDOServiceMock extends SDOServicePOA {
    };
    private class RTObject_impl_Mock extends RTObject_impl {
        public RTObject_impl_Mock(Manager manager) {
            super(manager);
        }
        public RTObject_impl_Mock(ORB orb, POA poa) {
            super(orb,poa);
        }
        public Configuration_impl getConfiguration_impl(){
            return m_pSdoConfigImpl;
        }

    }

    protected void setUp() throws Exception {
        super.setUp();
/*
        java.io.File fileCurrent = new java.io.File(".");
        String rootPath = fileCurrent.getAbsolutePath();
        rootPath = rootPath.substring(0,rootPath.length()-1);
        String testPath = rootPath + "tests\\src\\jp\\go\\aist\\rtm\\RTC\\sample\\rtc_nologger.conf";
        String param[] = {"-f", testPath };
        Manager manager = Manager.init(param);
//      Manager manager = Manager.instance();
        manager.activateManager();
        m_pConf = new Configuration_impl(new ConfigAdmin(new Properties()));
//        manager.m_objManager.activate(m_pConf);
        m_orb = manager.getORB();
*/

        String[] argv = {"-o","sdo.service.consumer.enabled_services:IDL:OpenRTM/ComponentObserver:1.0,INTERFACE_TYPE 0,INTERFACE_TYPE 1","-o","logger.enable:NO"};  
        Manager manager = Manager.init(argv);
	java.util.Properties props = new java.util.Properties();
        props.put("org.omg.CORBA.ORBInitialPort", "2809");
        props.put("org.omg.CORBA.ORBInitialHost", "localhost");
        m_orb = ORB.init(new String[0], props);
        m_poa = manager.getPOA();
        manager.getPOAManager().activate();
        RTObject_impl_Mock rtobj = new RTObject_impl_Mock(manager);

        m_pConf = rtobj.getConfiguration_impl();
        m_sdoservice = new SdoServiceAdmin(rtobj);

    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * <p>set_device_profile()メソッドとgetDeviceProfile()メソッドのテスト
     * <ul>
     * <li>set_device_profile()で設定したDeviceProfileを、getDeviceProfile()で正しく取得できるか？</li>
     * </ul>
     * </p>
     */
    public void test_set_device_profile_and_getDeviceProfile() throws Exception {
        Properties cfgAdminProp = new Properties();
        ConfigAdmin cfgAdmin = new ConfigAdmin(cfgAdminProp);
        Configuration_impl sdoCfg = new Configuration_impl(cfgAdmin,null);
        
        // DeviceProfileを準備する
        DeviceProfile devProf = new DeviceProfile();
        devProf.device_type = "DEVICE_TYPE";
        devProf.manufacturer = "MANUFACTURER";
        devProf.model = "MODEL";
        devProf.version = "VERSION";
        NameValue[] properties = new NameValue[2];
        properties[0] = new NameValue();
        properties[0].name = "name 0";
        Any value = ORBUtil.getOrb().create_any();
        value.insert_float(3.14159f);
        properties[0].value = value;
        properties[1] = new NameValue();
        properties[1].name = "name 1";
        Any value2 = ORBUtil.getOrb().create_any();
        value2.insert_float(2.71828f);
        properties[1].value = value2;
        devProf.properties = properties;
        
        // set_device_profile()を呼出して、準備したDeviceProfileを設定する
        assertTrue(sdoCfg.set_device_profile(devProf));
        
        // getDeviceProfile()で設定されているDeviceProfileを取り出し、設定したものと一致することを確認する
        DeviceProfile devProfRet = sdoCfg.getDeviceProfile();
        assertEquals("DEVICE_TYPE", devProfRet.device_type);
        assertEquals("MANUFACTURER", devProfRet.manufacturer);
        assertEquals("MODEL", devProfRet.model);
        assertEquals("VERSION", devProfRet.version);
        assertEquals("name 0", devProfRet.properties[0].name);
        float valuer = devProfRet.properties[0].value.extract_float();
        assertEquals(3.14159f, valuer);
        assertEquals("name 1", devProfRet.properties[1].name);
        float valuer2 = devProfRet.properties[1].value.extract_float();
        assertEquals(2.71828f, valuer2);
    }
    /**
     * <p>set_service_profile()メソッドとgetServiceProfile()メソッドのテスト
     * <ul>
     * <li>set_service_profile()で設定したServiceProfileを、getServiceProfile()で正しく取得できるか？</li>
     * </ul>
     * </p>
     */
    public void test_set_service_profile_and_getServiceProfile() throws Exception {
        Properties cfgAdminProp = new Properties();
        ConfigAdmin cfgAdmin = new ConfigAdmin(cfgAdminProp);
        Configuration_impl sdoCfg = m_pConf;
        
        // ServiceProfileを準備する
        ServiceProfile svcProf = new ServiceProfile();
        svcProf.id = "ID";
        svcProf.interface_type = "IDL:OpenRTM/ComponentObserver:1.0";
        ComponentObserverMock sdoSvc1 = new ComponentObserverMock();
        this.m_poa.activate_object(sdoSvc1);
        svcProf.service = sdoSvc1._this();
        NameValue[] properties = new NameValue[2];
        properties[0] = new NameValue();
        properties[0].name = "name 0";
        Any value = ORBUtil.getOrb().create_any();
        value.insert_float(3.14159f);
        properties[0].value = value;
        properties[1] = new NameValue();
        properties[1].name = "name 1";
        Any value2 = ORBUtil.getOrb().create_any();
        value2.insert_float(2.71828f);
        properties[1].value = value2;
        svcProf.properties = properties;
        
        // ServiceProfileを設定する
        assertTrue(sdoCfg.add_service_profile(svcProf));
        
    }
    /**
     * <p>remove_service_profile()メソッドのテスト
     * <ul>
     * <li>指定したIDを持つServiceProfileを正しく登録解除できるか？</li>
     * </ul>
     * </p>
     */
    public void test_remove_service_profile() throws Exception {
        Properties cfgAdminProp = new Properties();
        ConfigAdmin cfgAdmin = new ConfigAdmin(cfgAdminProp);
        Configuration_impl sdoCfg = m_pConf;
        
        // ServiceProfileを準備する
        ServiceProfile svcProf0 = new ServiceProfile();
        svcProf0.id = "ID 0";
        svcProf0.interface_type = "IDL:OpenRTM/ComponentObserver:1.0";
        ComponentObserverMock sdoSvc1 = new ComponentObserverMock();
        this.m_poa.activate_object(sdoSvc1);
        svcProf0.service = sdoSvc1._this();
        NameValue[] properties = new NameValue[2];
        properties[0] = new NameValue();
        properties[0].name = "name 0-0";
        Any value = ORBUtil.getOrb().create_any();
        value.insert_float(3.14159f);
        properties[0].value = value;
        properties[1] = new NameValue();
        properties[1].name = "name 0-1";
        Any value2 = ORBUtil.getOrb().create_any();
        value2.insert_float(2.71828f);
        properties[1].value = value2;
        svcProf0.properties = properties;
    
        ServiceProfile svcProf1 = new ServiceProfile();
        svcProf1.id = "ID 1";
        svcProf1.interface_type = "IDL:OpenRTM/ComponentObserver:1.0";
        ComponentObserverMock sdoSvc2 = new ComponentObserverMock();
        this.m_poa.activate_object(sdoSvc2);
        svcProf1.service = sdoSvc2._this();
        NameValue[] properties2 = new NameValue[2];
        properties2[0] = new NameValue();
        properties2[0].name = "name 1-0";
        Any value3 = ORBUtil.getOrb().create_any();
        value3.insert_float(1.41421356f);
        properties2[0].value = value3;
        properties2[1] = new NameValue();
        properties2[1].name = "name 1-1";
        Any value4 = ORBUtil.getOrb().create_any();
        value4.insert_float(1.7320508f);
        properties2[1].value = value4;
        svcProf1.properties = properties2;
        
        // ServiceProfileを設定する
        assertTrue(sdoCfg.add_service_profile(svcProf0));
        assertTrue(sdoCfg.add_service_profile(svcProf1));
        // 設定したServiceProfileのうち、片方を登録解除する
        assertTrue(sdoCfg.remove_service_profile("ID 0"));
        
    }
    /**
     * <p>add_organization()メソッドとgetOrganizations()メソッドのテスト
     * <ul>
     * <li>add_organization()でOrganization_ptrインスタンスを登録できるか？</li>
     * <li>getOrganizations()で登録されているOrganization_ptrインスタンス群を取得できるか？</li>
     * </ul>
     * </p>
     */
    public void test_add_organization_and_getOrganizations() throws Exception {
        Properties cfgAdminProp = new Properties();
        ConfigAdmin cfgAdmin = new ConfigAdmin(cfgAdminProp);
        Configuration_impl sdoCfg = new Configuration_impl(cfgAdmin,null);
        
        // Organizationを2つ登録する
        Organization org1 = new _OrganizationStub();
        assertTrue(sdoCfg.add_organization(org1));

        Organization org2 = new _OrganizationStub();
        assertTrue(sdoCfg.add_organization(org2));
  
        // 取得されるOrganizationの数は、意図どおり2つか？
        OrganizationListHolder orgList = sdoCfg.getOrganizations();
        assertEquals(2, orgList.value.length);
    }
    /**
     * <p>add/get_configuration_set()メソッドのテスト
     * <ul>
     * <li>ConfigurationSetをadd_configuration_set()で正常に登録できるか？</li>
     * <li>add_configuration_set()で登録したConfigurationSetを、get_configuration_set()で正しく取得できるか？</li>
     * </ul>
     * </p>
     */
    public void test_add_configuration_set_and_get_configuration_set() throws Exception {
        Properties cfgAdminProp = new Properties();
        ConfigAdmin cfgAdmin = new ConfigAdmin(cfgAdminProp);
        Configuration_impl sdoCfg = new Configuration_impl(cfgAdmin,null);
        
        // ConfigurationSetを準備する
        ConfigurationSet cfgSet0 = new ConfigurationSet();
        cfgSet0.id = "ID 0";
        cfgSet0.description = "DESCRIPTION 0";
        cfgSet0.configuration_data = new NameValue[2];
        cfgSet0.configuration_data[0] = new NameValue();
        cfgSet0.configuration_data[0].name = "NAME 0-0";
        Any value = ORBUtil.getOrb().create_any();
        value.insert_string("3.14159");
        cfgSet0.configuration_data[0].value = value;
        cfgSet0.configuration_data[1] = new NameValue();
        cfgSet0.configuration_data[1].name = "NAME 0-1";
        Any value2 = ORBUtil.getOrb().create_any();
        value2.insert_string("2.71828");
        cfgSet0.configuration_data[1].value = value2;
        
        ConfigurationSet cfgSet1 = new ConfigurationSet();
        cfgSet1.id = "ID 1";
        cfgSet1.description = "DESCRIPTION 1";
        cfgSet1.configuration_data = new NameValue[2];
        cfgSet1.configuration_data[0] = new NameValue();
        cfgSet1.configuration_data[0].name = "NAME 1-0";
        Any value3 = ORBUtil.getOrb().create_any();
        value3.insert_string("1.41421356");
        cfgSet1.configuration_data[0].value = value3;
        cfgSet1.configuration_data[1] = new NameValue();
        cfgSet1.configuration_data[1].name = "NAME 1-1";
        Any value4 = ORBUtil.getOrb().create_any();
        value4.insert_string("1.7320508");
        cfgSet1.configuration_data[1].value = value4;
        
        // 準備したConfigurationSetを登録する
        assertTrue(sdoCfg.add_configuration_set(cfgSet0));
        assertTrue(sdoCfg.add_configuration_set(cfgSet1));
        
        // 登録したConfigurationSetを正しく取得できるか？
        ConfigurationSet cfgSetRet0 = sdoCfg.get_configuration_set("ID 0");
        assertEquals("ID 0", cfgSetRet0.id);
        assertEquals(2, cfgSetRet0.configuration_data.length);
        assertEquals("NAME 0-0", cfgSetRet0.configuration_data[0].name);
        String valuer;
        if( cfgSetRet0.configuration_data[0].value.type().kind() == TCKind.tk_wstring ) {
            valuer = cfgSetRet0.configuration_data[0].value.extract_wstring();
        } else {
            valuer = cfgSetRet0.configuration_data[0].value.extract_string();
        }
        assertEquals("3.14159", valuer);
        
        assertEquals("NAME 0-1", cfgSetRet0.configuration_data[1].name);
        String valuer2;
        if( cfgSetRet0.configuration_data[1].value.type().kind() == TCKind.tk_wstring ) {
            valuer2 = cfgSetRet0.configuration_data[1].value.extract_wstring();
        } else {
            valuer2 = cfgSetRet0.configuration_data[1].value.extract_string();
        }
        assertEquals("2.71828",valuer2);

        ConfigurationSet cfgSetRet1 = sdoCfg.get_configuration_set("ID 1");
        assertEquals("ID 1", cfgSetRet1.id);
        assertEquals(2, cfgSetRet1.configuration_data.length);
        assertEquals("NAME 1-0", cfgSetRet1.configuration_data[0].name);
        String valuer3;
        if( cfgSetRet1.configuration_data[0].value.type().kind() == TCKind.tk_wstring ) {
            valuer3 = cfgSetRet1.configuration_data[0].value.extract_wstring();
        } else {
            valuer3 = cfgSetRet1.configuration_data[0].value.extract_string();
        }
        assertEquals("1.41421356", valuer3);
        //
        assertEquals("NAME 1-1", cfgSetRet1.configuration_data[1].name);
        String valuer4;
        if( cfgSetRet1.configuration_data[1].value.type().kind() == TCKind.tk_wstring ) {
            valuer4 = cfgSetRet1.configuration_data[1].value.extract_wstring();
        } else {
            valuer4 = cfgSetRet1.configuration_data[1].value.extract_string();
        }
        assertEquals("1.7320508", valuer4);
    }
    /**
     * <p>remove_configuration_set()メソッドのテスト
     * <ul>
     * <li>登録済みのConfigurationSetを正しく登録解除できるか？</li>
     * <li>登録されていないIDを指定した場合、意図どおり例外がスローされるか？</li>
     * </ul>
     * </p>
     */
    public void test_remove_configuration_set() throws Exception {
        Properties cfgAdminProp = new Properties();
        ConfigAdmin cfgAdmin = new ConfigAdmin(cfgAdminProp);
        Configuration_impl sdoCfg = new Configuration_impl(cfgAdmin,null);
        
        // ConfigurationSetを準備する
        ConfigurationSet cfgSet0 = new ConfigurationSet();
        cfgSet0.id = "ID 0";
        cfgSet0.description = "DESCRIPTION 0";
        cfgSet0.configuration_data = new NameValue[2];
        cfgSet0.configuration_data[0] = new NameValue();
        cfgSet0.configuration_data[0].name = "NAME 0-0";
        Any value = ORBUtil.getOrb().create_any();
        value.insert_string("3.14159");
        cfgSet0.configuration_data[0].value = value;
        cfgSet0.configuration_data[1] = new NameValue();
        cfgSet0.configuration_data[1].name = "NAME 0-1";
        Any value2 = ORBUtil.getOrb().create_any();
        value2.insert_string("2.71828");
        cfgSet0.configuration_data[1].value = value2;
        
        ConfigurationSet cfgSet1 = new ConfigurationSet();
        cfgSet1.id = "ID 1";
        cfgSet1.description = "DESCRIPTION 1";
        cfgSet1.configuration_data = new NameValue[2];
        cfgSet1.configuration_data[0] = new NameValue();
        cfgSet1.configuration_data[0].name = "NAME 1-0";
        Any value3 = ORBUtil.getOrb().create_any();
        value3.insert_string("1.41421356");
        cfgSet1.configuration_data[0].value = value3;
        cfgSet1.configuration_data[1] = new NameValue();
        cfgSet1.configuration_data[1].name = "NAME 1-1";
        Any value4 = ORBUtil.getOrb().create_any();
        value4.insert_string("1.7320508");
        cfgSet1.configuration_data[1].value = value4;
        
        // 準備したConfigurationSetを登録する
        assertTrue(sdoCfg.add_configuration_set(cfgSet0));
        assertTrue(sdoCfg.add_configuration_set(cfgSet1));
        
        // 登録したうち、片方のConfigurationSetを登録解除する
        assertTrue(sdoCfg.remove_configuration_set("ID 0"));
        
        // 登録解除したConfigurationSetを指定して、get_configuration_set()呼出を試みて、
        // 意図どおりに例外がスローされるか？
        try {
            sdoCfg.get_configuration_set("ID 0");
            fail("ID 0 was not removed.");
        } catch (InternalError expected) {}
        
        // 登録解除していないConfigurationSetは、依然として取得できるか？
        ConfigurationSet cfgSetRet = sdoCfg.get_configuration_set("ID 1");
        assertEquals("ID 1", cfgSetRet.id);
        
        // 存在しないIDを指定して登録解除を試みた場合に、意図どおりに例外がスローされるか？
        try {
            sdoCfg.remove_configuration_set("inexist ID");
            fail("Exception not thrown.");
        } catch (InternalError expected) {}
    }
    /**
     * <p>set_configuration_set_values()メソッドのテスト
     * <ul>
     * <li>登録済みのConfigurationSetのIDを指定して、正しくConfigurationSetを更新できるか？</li>
     * <li>存在しないIDを指定した場合に、意図どおりに例外がスローされるか？</li>
     * </ul>
     * </p>
     */
    public void test_set_configuration_set_values2() throws Exception {
        Properties cfgAdminProp = new Properties();
        ConfigAdmin cfgAdmin = new ConfigAdmin(cfgAdminProp);
        Configuration_impl sdoCfg = new Configuration_impl(cfgAdmin,null);
        
        // ConfigurationSetを準備する
        ConfigurationSet cfgSet0 = new ConfigurationSet();
        cfgSet0.id = "ID 0";
        cfgSet0.description = "DESCRIPTION 0";
        cfgSet0.configuration_data = new NameValue[2];
        cfgSet0.configuration_data[0] = new NameValue();
        cfgSet0.configuration_data[0].name = "NAME 0-0";
        Any value = ORBUtil.getOrb().create_any();
        value.insert_string("3.14159");
        cfgSet0.configuration_data[0].value = value;
        cfgSet0.configuration_data[1] = new NameValue();
        cfgSet0.configuration_data[1].name = "NAME 0-1";
        Any value2 = ORBUtil.getOrb().create_any();
        value2.insert_string("2.71828");
        cfgSet0.configuration_data[1].value = value2;
        
        ConfigurationSet cfgSet1 = new ConfigurationSet();
        cfgSet1.id = "ID 1";
        cfgSet1.description = "DESCRIPTION 1";
        cfgSet1.configuration_data = new NameValue[2];
        cfgSet1.configuration_data[0] = new NameValue();
        cfgSet1.configuration_data[0].name = "NAME 1-0";
        Any value3 = ORBUtil.getOrb().create_any();
        value3.insert_string("1.41421356");
        cfgSet1.configuration_data[0].value = value3;
        cfgSet1.configuration_data[1] = new NameValue();
        cfgSet1.configuration_data[1].name = "NAME 1-1";
        Any value4 = ORBUtil.getOrb().create_any();
        value4.insert_string("1.7320508");
        cfgSet1.configuration_data[1].value = value4;

        ConfigurationSet cfgSet1_Modified = new ConfigurationSet();
        cfgSet1_Modified.id = "ID 1";
        cfgSet1_Modified.description = "DESCRIPTION 1 M";
        cfgSet1_Modified.configuration_data = new NameValue[2];
        cfgSet1_Modified.configuration_data[0] = new NameValue();
        cfgSet1_Modified.configuration_data[0].name = "NAME 1-0 M";
        Any value5 = ORBUtil.getOrb().create_any();
        value5.insert_string("2.23620679");
        cfgSet1_Modified.configuration_data[0].value = value5;
        cfgSet1_Modified.configuration_data[1] = new NameValue();
        cfgSet1_Modified.configuration_data[1].name = "NAME 1-1 M";
        Any value6 = ORBUtil.getOrb().create_any();
        value6.insert_string("2.44948974");
        cfgSet1_Modified.configuration_data[1].value = value6;

        // 準備したConfigurationSetを登録する
        assertTrue(sdoCfg.add_configuration_set(cfgSet0));
        assertTrue(sdoCfg.add_configuration_set(cfgSet1));
        
        // 登録したConfigurationSetのうち片方を、set_configuration_set_values()で更新する
        assertTrue(sdoCfg.set_configuration_set_values(cfgSet1_Modified));
        
        // 更新したConfigurationSetを、正しく取得できるか？
        ConfigurationSet cfgSetRet = sdoCfg.get_configuration_set("ID 1");
        assertEquals("ID 1", cfgSetRet.id);
        assertEquals(4, cfgSetRet.configuration_data.length);
        assertEquals("NAME 1-0", cfgSetRet.configuration_data[0].name);
        String valuer;
        if( cfgSetRet.configuration_data[0].value.type().kind() == TCKind.tk_wstring ) {
            valuer = cfgSetRet.configuration_data[0].value.extract_wstring();
        } else {
            valuer = cfgSetRet.configuration_data[0].value.extract_string();
        }
        assertEquals("1.41421356", valuer);
        assertEquals("NAME 1-1", cfgSetRet.configuration_data[1].name);
        String valuer2;
        if( cfgSetRet.configuration_data[1].value.type().kind() == TCKind.tk_wstring ) {
            valuer2 = cfgSetRet.configuration_data[1].value.extract_wstring();
        } else {
            valuer2 = cfgSetRet.configuration_data[1].value.extract_string();
        }
        assertEquals("1.7320508", valuer2);
        assertEquals("NAME 1-0 M", cfgSetRet.configuration_data[2].name);
        String valuer3;
        if( cfgSetRet.configuration_data[2].value.type().kind() == TCKind.tk_wstring ) {
            valuer3 = cfgSetRet.configuration_data[2].value.extract_wstring();
        } else {
            valuer3 = cfgSetRet.configuration_data[2].value.extract_string();
        }
        assertEquals("2.23620679", valuer3);
        assertEquals("NAME 1-1 M", cfgSetRet.configuration_data[3].name);
        String valuer4;
        if( cfgSetRet.configuration_data[3].value.type().kind() == TCKind.tk_wstring ) {
            valuer4 = cfgSetRet.configuration_data[3].value.extract_wstring();
        } else {
            valuer4 = cfgSetRet.configuration_data[3].value.extract_string();
        }
        assertEquals("2.44948974", valuer4);
        
        // 存在しないIDを指定してset_configuration_set_values()を呼出し、
        // 意図どおり例外がスローされるか？
        try {
            sdoCfg.get_configuration_set("inexist ID");
            fail("Exception not thrown.");
        } catch (InternalError expected) {}
    }
    /**
     * <p>activate_configuration_set()メソッドのテスト
     * <ul>
     * <li></li>
     * </ul>
     * </p>
     */
    public void test_activate_configuration_set_and_get_active_configuration_set() throws Exception {
        Properties cfgAdminProp = new Properties();
        ConfigAdmin cfgAdmin = new ConfigAdmin(cfgAdminProp);
        Configuration_impl sdoCfg = new Configuration_impl(cfgAdmin,null);
        
        // ConfigurationSetを準備する
        ConfigurationSet cfgSet0 = new ConfigurationSet();
        cfgSet0.id = "ID 0";
        cfgSet0.description = "DESCRIPTION 0";
        cfgSet0.configuration_data = new NameValue[2];
        cfgSet0.configuration_data[0] = new NameValue();
        cfgSet0.configuration_data[0].name = "NAME 0-0";
        Any value = ORBUtil.getOrb().create_any();
        value.insert_string("3.14159");
        cfgSet0.configuration_data[0].value = value;
        cfgSet0.configuration_data[1] = new NameValue();
        cfgSet0.configuration_data[1].name = "NAME 0-1";
        Any value2 = ORBUtil.getOrb().create_any();
        value2.insert_string("2.71828");
        cfgSet0.configuration_data[1].value = value2;
        
        ConfigurationSet cfgSet1 = new ConfigurationSet();
        cfgSet1.id = "ID 1";
        cfgSet1.description = "DESCRIPTION 1";
        cfgSet1.configuration_data = new NameValue[2];
        cfgSet1.configuration_data[0] = new NameValue();
        cfgSet1.configuration_data[0].name = "NAME 1-0";
        Any value3 = ORBUtil.getOrb().create_any();
        value3.insert_string("1.41421356");
        cfgSet1.configuration_data[0].value = value3;
        cfgSet1.configuration_data[1] = new NameValue();
        cfgSet1.configuration_data[1].name = "NAME 1-1";
        Any value4 = ORBUtil.getOrb().create_any();
        value4.insert_string("1.7320508");
        cfgSet1.configuration_data[1].value = value4;

        // 準備したConfigurationSetを登録する
        assertTrue(sdoCfg.add_configuration_set(cfgSet0));
        assertTrue(sdoCfg.add_configuration_set(cfgSet1));
        
        // "ID 0"のほうをアクティブ化する
        assertTrue(sdoCfg.activate_configuration_set("ID 0"));
        
        // アクティブなConfigurationSetを取得し、それがアクティブ化したものと一致するか？
        ConfigurationSet cfgSetRet0 = sdoCfg.get_configuration_set("ID 0");
        assertEquals("ID 0", cfgSetRet0.id);
        assertEquals(2, cfgSetRet0.configuration_data.length);
        assertEquals("NAME 0-0", cfgSetRet0.configuration_data[0].name);
        String valuer;
        if( cfgSetRet0.configuration_data[0].value.type().kind() == TCKind.tk_wstring ) {
            valuer = cfgSetRet0.configuration_data[0].value.extract_wstring();
        } else {
            valuer = cfgSetRet0.configuration_data[0].value.extract_string();
        }
        assertEquals("3.14159", valuer);
        assertEquals("NAME 0-1", cfgSetRet0.configuration_data[1].name);
        String valuer2;
        if( cfgSetRet0.configuration_data[1].value.type().kind() == TCKind.tk_wstring ) {
            valuer2 = cfgSetRet0.configuration_data[1].value.extract_wstring();
        } else {
            valuer2 = cfgSetRet0.configuration_data[1].value.extract_string();
        }
        assertEquals("2.71828", valuer2);

        // "ID 0"のほうをアクティブ化する
        assertTrue(sdoCfg.activate_configuration_set("ID 0"));
        
        // アクティブなConfigurationSetを取得し、それがアクティブ化したものと一致するか？
        ConfigurationSet cfgSetRet1 = sdoCfg.get_configuration_set("ID 1");
        assertEquals("ID 1", cfgSetRet1.id);
        assertEquals(2, cfgSetRet1.configuration_data.length);
        assertEquals("NAME 1-0", cfgSetRet1.configuration_data[0].name);
        String valuer3;
        if( cfgSetRet0.configuration_data[0].value.type().kind() == TCKind.tk_wstring ) {
            valuer3 = cfgSetRet1.configuration_data[0].value.extract_wstring();
        } else {
            valuer3 = cfgSetRet1.configuration_data[0].value.extract_string();
        }
        assertEquals("1.41421356", valuer3);
        assertEquals("NAME 1-1", cfgSetRet1.configuration_data[1].name);
        String valuer4;
        if( cfgSetRet0.configuration_data[1].value.type().kind() == TCKind.tk_wstring ) {
            valuer4 = cfgSetRet1.configuration_data[1].value.extract_wstring();
        } else {
            valuer4 = cfgSetRet1.configuration_data[1].value.extract_string();
        }
        assertEquals("1.7320508", valuer4);
        
        // 存在しないIDを指定してactivate_configuration_set()を呼出し、意図どおりの例外がスローされるか？
        try {
            sdoCfg.activate_configuration_set("inexist ID");
            fail("Exception not thrown.");
        } catch (InvalidParameter expected) {}
    }

    
    
    /**
     * <p>SDO　DeviceProfileの設定/取得チェック
     * <ul>
     * <li>DeviceProfileを正常に設定できるか？</li>
     * <li>設定したDeviceProfileを正常に取得できるか？</li>
     * </ul>
     * </p>
     */
    public void test_set_device_profile() {
        short st, retst;
        int  lg, retlg;
        float ft, retft;
        NVListHolder nlist = new NVListHolder();
      
       // DeviceProfile.properties要素のセット
        nlist.value = new NameValue[3];
        st = 10;
        nlist.value[0] = new NameValue();
        nlist.value[0].name = "short data";
        Any anyValue = m_orb.create_any();
        anyValue.insert_short(st);
        nlist.value[0].value = anyValue;
      
        lg = 100000;
        nlist.value[1] = new NameValue();
        nlist.value[1].name = "long data";
        anyValue = m_orb.create_any();
        anyValue.insert_long(lg);
        nlist.value[1].value = anyValue;
      
        ft = 1234.5F;
        nlist.value[2] = new NameValue();
        nlist.value[2].name = "float data";
        anyValue = m_orb.create_any();
        anyValue.insert_float(ft);
        nlist.value[2].value = anyValue;
      
        // DeviceProfile要素のセット
        DeviceProfile setProf = new DeviceProfile();
        DeviceProfile retProf = new DeviceProfile();
        setProf.device_type = "Joystick";
        setProf.manufacturer = "Aist";
        setProf.model = "hoge";
        setProf.version = "0.4.0";
        setProf.properties = nlist.value;
      
        boolean result;
      
        // DeviceProfileのセット
        try {
            result = m_pConf.set_device_profile(setProf);
        } catch (InvalidParameter e) {
            e.printStackTrace();
            fail();
        } catch (NotAvailable e) {
            e.printStackTrace();
            fail();
        } catch (InternalError e) {
            e.printStackTrace();
            fail();
        }
      
        // DeviceProfileの取得
        retProf = m_pConf.getDeviceProfile();
        String retval;
      
        // セットしたDeviceProfileの要素と取得したDeviceProfileの要素を比較。
        retval = retProf.device_type;
        assertEquals("Joystick", retval);
      
        retval = retProf.manufacturer;
        assertEquals("Aist", retval);
      
        retval = retProf.model;
        assertEquals("hoge", retval);
      
        retval = retProf.version;
        assertEquals("0.4.0", retval);

        retval = retProf.properties[0].name;
        assertEquals("short data", retval);
        retst = retProf.properties[0].value.extract_short();
        assertEquals(10, retst);

        retval = retProf.properties[1].name;
        assertEquals("long data", retval);
        retlg = retProf.properties[1].value.extract_long();
        assertEquals(100000, lg);
      
        retval = retProf.properties[2].name;
        assertEquals("float data", retval);
        retft = retProf.properties[2].value.extract_float();
        assertEquals(1234.5F, ft);
    }

    /**
     * <p>SDO　サービスプロファイルの設定/取得チェック
     * <ul>
     * <li>空のServiceProfileを設定できるか？</li>
     * </ul>
     * </p>
     */
    public void test_set_null_service_profile() {
        boolean result;
        ServiceProfile sPro = new ServiceProfile();
        // ServiceProfileのセット
        try {
            result = m_pConf.add_service_profile(sPro);
        } catch (InvalidParameter e1) {
            e1.printStackTrace();
            fail();
        } catch (NotAvailable e1) {
            e1.printStackTrace();
            fail();
        } catch (InternalError e1) {
            e1.printStackTrace();
            fail();
        }
    
    }
    /**
     * <p>SDO　サービスプロファイルの設定/取得チェック
     * <ul>
     * <li>ServiceProfileを正常に設定できるか？</li>
     * <li>IDを指定してServiceProfileを取得できるか？</li>
     * <li>存在しないIDを指定してServiceProfileを取得した場合，空のプロファイルが返ってくるか？</li>
     * <li>全ServiceProfileを取得できるか？</li>
     * <li>IDを指定してServiceProfileを削除できるか？</li>
     * </ul>
     * </p>
     */
    public void test_set_service_profile() {
        ServiceProfile getProf;
        ServiceProfile svcProf0 = new ServiceProfile();
        ServiceProfile svcProf1 = new ServiceProfile();;
        NameValue nv = new NameValue();
        NVListHolder nvlist = new NVListHolder();
        short setst,getst;
        int  setlg,getlg;
        boolean result;
      
        String setstr, getstr;
      
        // ServiceProfile.properties要素のセット
        setst = 10; 
        nv.name = "short";
        Any anyValue = m_orb.create_any();
        anyValue.insert_short(setst);
        nv.value = anyValue;
        nvlist.value = new NameValue[1];
        nvlist.value[0] = nv;
        // ServiceProfileの要素セット
        svcProf0.id = "setProfId0";
        svcProf0.interface_type = "ifTYpe0";
        svcProf0.properties = nvlist.value;
      
        // ServiceProfileのセット
        try {
            result = m_pConf.add_service_profile(svcProf0);
        } catch (InvalidParameter e1) {
            e1.printStackTrace();
            fail();
        } catch (NotAvailable e1) {
            e1.printStackTrace();
            fail();
        } catch (InternalError e1) {
            e1.printStackTrace();
            fail();
        }
        // ServiceProfile.properties要素のセット
        setlg = 1000;
        NameValue nv2 = new NameValue();
        nv2.name = "long";
        anyValue = m_orb.create_any();
        anyValue.insert_long(setlg);
        nv2.value = anyValue;
        NVListHolder nvlist2 = new NVListHolder();
        nvlist2.value = new NameValue[1];
        nvlist2.value[0] = nv2;
        // ServiceProfileの要素セット
        svcProf1.id = "setProfId1";
        svcProf1.interface_type = "ifTYpe1";
        svcProf1.properties = nvlist2.value;
        
        // ServiceProfileのセット
        try {
            m_pConf.add_service_profile(svcProf1);
        } catch (InvalidParameter e1) {
            e1.printStackTrace();
            fail();
        } catch (NotAvailable e1) {
            e1.printStackTrace();
            fail();
        } catch (InternalError e1) {
            e1.printStackTrace();
            fail();
        }
      

      //================================================================
      
      // ServiceProfileListから引数で与えたidを持つ
      // ServiceProfileの削除を行う
        try {
            m_pConf.remove_service_profile(svcProf0.id);
        } catch (InvalidParameter e) {
            e.printStackTrace();
            fail();
        } catch (NotAvailable e) {
            e.printStackTrace();
            fail();
        } catch (InternalError e) {
            e.printStackTrace();
            fail();
        }
        try {
            m_pConf.remove_service_profile(svcProf0.id);
        } catch (InvalidParameter e) {
            e.printStackTrace();
            fail();
        } catch (NotAvailable e) {
            e.printStackTrace();
            fail();
        } catch (InternalError e) {
            e.printStackTrace();
            fail();
        }

      //==================================================================
    }

    /**
     * <p>SDO　コンフィギュレーション パラメータの設定/取得チェック
     * <ul>
     * <li>初期状態で取得した場合，空のリストが返ってくるか？</li>
     * </ul>
     * </p>
     */
    public void test_get_configuration_parameters() {
      ParameterListHolder paramList = new ParameterListHolder();
      
      // length 0のリストが戻される。 OK.
      try {
        paramList.value = m_pConf.get_configuration_parameters();
    } catch (NotAvailable e) {
        e.printStackTrace();
        fail();
    } catch (InternalError e) {
        e.printStackTrace();
        fail();
    }
      int length = paramList.value.length;
      assertEquals(0, length);
    }

    /**
     * <p>SDO　コンフィギュレーションセットのチェック
     * <ul>
     * <li>ConfigurationSetを正常に追加できるか？</li>
     * <li>全ConfigurationSetを取得できるか？</li>
     * <li>引数で指定したConfigurationSetを取得できるか？</li>
     * <li>コンフィギュレーションセット中の個別値を直接更新できるか？</li>
     * <li>コンフィギュレーションセットを正常にActive化できるか？</li>
     * <li>Activeなコンフィギュレーションセットを取得できるか？</li>
     * <li>IDを指定してコンフィギュレーションセットを削除できるか？</li>
     * </ul>
     * </p>
     */
    public void test_get_configuration_sets() {
      ConfigurationSet confset = new ConfigurationSet();
      ConfigurationSet getconfset = new ConfigurationSet();
      NVListHolder nvlist = new NVListHolder();
      NameValue nv = new NameValue();
      short st, rst;
      int lg, rlg;
      float ft, rft;
      boolean result;
      String setname, getname;
      Any any;
      int llength;
      
      //============ ConfigurationSet1 =================================
      // ConfigurationSet要素のセット
      confset.id = "configset_id1";
      confset.description = "configset_description";
      nvlist.value = new NameValue[1];
      nv.name = "short";
      st = 10000;
      ShortHolder sth = new ShortHolder(st);
      Any anyValue = m_orb.create_any();
      anyValue.insert_Value(sth);
      nv.value = anyValue;
      nvlist.value[0] = new NameValue();
      nvlist.value[0] = nv;
      confset.configuration_data = nvlist.value;
      //================================================================
      
      // ConfigurationSetの追加
      try {
        result = m_pConf.add_configuration_set(confset);
        assertTrue(result);
        assertEquals( 1, m_pConf.get_configuration_sets().length );
      
      // Activate ConfigurationSet1.
      result = m_pConf.activate_configuration_set(confset.id);
      
      
      //============ ConfigurationSet2 =================================
      // ConfigurationSet要素のセット
      ConfigurationSet confset2 = new ConfigurationSet();
      confset2.id = "configset_id2";
      confset2.description = "configset_description2";
      NVListHolder nvlist2 = new NVListHolder();
      nvlist2.value = new NameValue[1];
      NameValue nv2 = new NameValue();
      nv2.name = "long";
      lg = 20000;
      IntegerHolder lgh = new IntegerHolder(lg);
      anyValue = m_orb.create_any();
      anyValue.insert_Value(lgh);
      nv2.value = anyValue;
      nvlist2.value[0] = nv2;
      confset2.configuration_data = nvlist2.value;
      //================================================================
      
      // ConfigurationSetの追加
      result = m_pConf.add_configuration_set(confset2);
      
      // Activate ConfigurationSet2.
      result = m_pConf.activate_configuration_set(confset2.id);
      
      //================= get_configuration_sets()のテスト ==============
      ConfigurationSetListHolder confSetList = new ConfigurationSetListHolder(new ConfigurationSet[0]);
      confSetList.value = m_pConf.get_configuration_sets();
      llength = confSetList.value.length;
      assertEquals(2, llength);
      
      getname = confSetList.value[0].id;
      assertEquals("configset_id1", getname);
      Any getAny = confSetList.value[0].configuration_data[0].value;
      if( getAny.type().kind() == TCKind.tk_wstring ) {
          rst = Short.valueOf(getAny.extract_wstring());
      } else {
          rst = Short.valueOf(getAny.extract_string());
      }
      assertEquals(10000, rst);
      
      getname = confSetList.value[1].id;
      assertEquals("configset_id2", getname);
      getAny = confSetList.value[1].configuration_data[0].value;
      if( getAny.type().kind() == TCKind.tk_wstring ) {
          rlg = Integer.valueOf(getAny.extract_wstring());
      } else {
          rlg = Integer.valueOf(getAny.extract_string());
      }
      assertEquals(20000, rlg);
      //=================================================================
      
      //=============== get_configuration_set()のテスト ==================
      ConfigurationSet confSet3;
      confSet3 = m_pConf.get_configuration_set("configset_id1");
      
      getname = confSet3.id;
      assertEquals("configset_id1", getname);
      getAny = confSet3.configuration_data[0].value;
      if( getAny.type().kind() == TCKind.tk_wstring ) {
          rst = Short.valueOf(getAny.extract_wstring());
      } else {
          rst = Short.valueOf(getAny.extract_string());
      }
      assertEquals(10000, rst);
      
      confSet3 = m_pConf.get_configuration_set("configset_id2");
      
      getname = confSet3.id;
      assertEquals("configset_id2", getname);
      getAny = confSet3.configuration_data[0].value;
      if( getAny.type().kind() == TCKind.tk_wstring ) {
          rlg = Integer.valueOf(getAny.extract_wstring());
      } else {
          rlg = Integer.valueOf(getAny.extract_string());
      }
      assertEquals(20000, rlg);
      //==================================================================
      
      //========== set_configuration_set_values()のテスト ===============
      ConfigurationSet confSet4 = new ConfigurationSet();
      confSet4.id = "configset_id2";
      confSet4.description = "changed configset_description.";
      NVListHolder nvlist3 = new NVListHolder();
      nvlist3.value = new NameValue[1];
      NameValue nv3 = new NameValue();
      nv3.name = "float";
      ft = 999.999F;
      FloatHolder fth = new FloatHolder(ft);
      anyValue = m_orb.create_any();
      anyValue.insert_Value(fth);
      nv3.value = anyValue;
      nvlist3.value[0] = nv3;
      confSet4.configuration_data = nvlist3.value;
      result = m_pConf.set_configuration_set_values(confSet4);
      
      // ConfigurationSetが正しくセットされているかを確認するため
      // get_configuration_set()を使用。
      getconfset = m_pConf.get_configuration_set("configset_id2");
      getname = getconfset.id;
      assertEquals("configset_id2", getname);
      getAny = getconfset.configuration_data[1].value;
      if( getAny.type().kind() == TCKind.tk_wstring ) {
          rft = Float.valueOf(getAny.extract_wstring());
      } else {
          rft = Float.valueOf(getAny.extract_string());
      }
      assertEquals(999.999F, rft);
      
      // ConfigurationSetが正しくセットされているかを確認するため
      // get_configuration_sets()を使用。
      confSetList.value = m_pConf.get_configuration_sets();
      llength = confSetList.value.length;
      assertEquals(llength, 2);
      
      getname = confSetList.value[0].id;
      assertEquals("configset_id1", getname);
      getAny = confSetList.value[0].configuration_data[0].value;
      if( getAny.type().kind() == TCKind.tk_wstring ) {
          rst = Short.valueOf(getAny.extract_wstring());
      } else {
          rst = Short.valueOf(getAny.extract_string());
      }
      assertEquals(10000, rst);
      
      getname = confSetList.value[1].id;
      assertEquals("configset_id2", getname);
      getAny = confSetList.value[1].configuration_data[0].value;
      if( getAny.type().kind() == TCKind.tk_wstring ) {
          rft = Float.valueOf(getAny.extract_wstring());
      } else {
          rft = Float.valueOf(getAny.extract_string());
      }
      assertEquals(20000.0F, rft);
      //===================================================================
      
      //=========== get_active_configuration_set()のテスト ===============
      getconfset = m_pConf.get_active_configuration_set();
      getname = getconfset.id;
      assertEquals("configset_id2", getname);
      
      String getdesc = getconfset.description;
      //===================================================================
      
      //=========== remove_configuration_set()のテスト ===================
      result = m_pConf.activate_configuration_set("configset_id1");
      result = m_pConf.remove_configuration_set("configset_id2");
      // "configset_id2"を要素に持つConfigurationSetが削除されているか
      // を確認するため get_configuration_sets()を使用。
      confSetList.value = m_pConf.get_configuration_sets();
      llength = confSetList.value.length;
      assertEquals(llength, 1);
      
      getconfset = m_pConf.get_configuration_set("configset_id1");
      
      getname = getconfset.id;
      assertEquals("configset_id1", getname);
      getAny = confSetList.value[0].configuration_data[0].value;
      if( getAny.type().kind() == TCKind.tk_wstring ) {
          rft = Float.valueOf(getAny.extract_wstring());
      } else {
          rst = Short.valueOf(getAny.extract_string());
      }
      assertEquals(10000, rst);
      //===================================================================
      } catch (InvalidParameter e) {
          e.printStackTrace();
          fail();
      } catch (NotAvailable e) {
          e.printStackTrace();
          fail();
      } catch (InternalError e) {
          e.printStackTrace();
          fail();
      }
    }

    public void test_set_configuration_set_values() {
        ConfigurationSet confset = new ConfigurationSet();
        ConfigurationSet getconfset = new ConfigurationSet();
        NVListHolder nvlist = new NVListHolder();
        NameValue nv = new NameValue();
        short st;
        int lg;
        boolean result;
        
        //============ ConfigurationSet1 =================================
        // ConfigurationSet要素のセット
        confset.id = "configset_id1";
        confset.description = "configset_description";
        nvlist.value = new NameValue[2];
        nv.name = "short";
        st = 10000;
        ShortHolder sth = new ShortHolder(st);
        Any anyValue = m_orb.create_any();
        anyValue.insert_Value(sth);
        nv.value = anyValue;
        nvlist.value[0] = new NameValue();
        nvlist.value[0] = nv;
        //
        nv = new NameValue();
        nv.name = "long";
        lg = 10;
        IntegerHolder lgh = new IntegerHolder(lg);
        anyValue = m_orb.create_any();
        anyValue.insert_Value(lgh);
        nv.value = anyValue;
        nvlist.value[1] = new NameValue();
        nvlist.value[1] = nv;
        //
        confset.configuration_data = nvlist.value;
        //================================================================
        
        // ConfigurationSetの追加
        try {
          result = m_pConf.add_configuration_set(confset);
          assertTrue(result);
          assertEquals( 1, m_pConf.get_configuration_sets().length );
          getconfset = m_pConf.get_configuration_set("configset_id1");
          assertEquals(2, getconfset.configuration_data.length);
        } catch (InvalidParameter e) {
            e.printStackTrace();
            fail();
        } catch (NotAvailable e) {
            e.printStackTrace();
            fail();
        } catch (InternalError e) {
            e.printStackTrace();
            fail();
        }
        
        //============ ConfigurationSet2 =================================
        // ConfigurationSet要素のセット
        ConfigurationSet confset2 = new ConfigurationSet();
        confset2.id = "configset_id1";
        confset2.description = "configset_description";
        NVListHolder nvlist2 = new NVListHolder();
        nvlist2.value = new NameValue[1];
        NameValue nv2 = new NameValue();
        nv2.name = "long";
        lg = 20000;
        lgh = new IntegerHolder(lg);
        anyValue = m_orb.create_any();
        anyValue.insert_Value(lgh);
        nv2.value = anyValue;
        nvlist2.value[0] = nv2;
        confset2.configuration_data = nvlist2.value;
        //================================================================
        
        try {
            result = m_pConf.set_configuration_set_values(confset2);
            assertTrue(result);
            assertEquals( 1, m_pConf.get_configuration_sets().length );
            getconfset = m_pConf.get_configuration_set("configset_id1");
            assertEquals(2, getconfset.configuration_data.length);
        } catch (InvalidParameter e) {
            e.printStackTrace();
            fail();
        } catch (NotAvailable e) {
            e.printStackTrace();
            fail();
        } catch (InternalError e) {
            e.printStackTrace();
            fail();
        }
    }
}
