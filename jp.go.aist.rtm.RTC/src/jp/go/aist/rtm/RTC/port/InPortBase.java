package jp.go.aist.rtm.RTC.port;

import java.util.Vector;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;
import java.util.Observer;

import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.TCKind;

import _SDOPackage.NVListHolder;
import RTC.ConnectorProfile;
import RTC.ConnectorProfileHolder;
import RTC.ReturnCode_t;

import jp.go.aist.rtm.RTC.BufferFactory;
import jp.go.aist.rtm.RTC.InPortProviderFactory;
import jp.go.aist.rtm.RTC.OutPortProviderFactory;
import jp.go.aist.rtm.RTC.OutPortConsumerFactory;
import jp.go.aist.rtm.RTC.buffer.BufferBase;
import jp.go.aist.rtm.RTC.buffer.RingBuffer;
import jp.go.aist.rtm.RTC.port.ReturnCode;
import jp.go.aist.rtm.RTC.util.Properties;
import jp.go.aist.rtm.RTC.util.NVUtil;
import jp.go.aist.rtm.RTC.util.StringUtil;
import jp.go.aist.rtm.RTC.util.CORBA_SeqUtil;

 
/**
 * <p> InPortBase </p>
 * <p> Port for InPort </p>
 *
 * <p> This is an implementation class for the data input port. <p>
 *
 */
public abstract class InPortBase extends PortBase {


    /**
     *
     * <p> Constructor </p>
     * @param name Port name
     * @param data_type Specify the data type used in the InPort object.
     */
    public InPortBase(final String name, final String data_type) {
        super(name);

        rtcout.println(rtcout.DEBUG, "Port name: "+name);

        m_singlebuffer = true;
        m_thebuffer = null;
        m_isLittleEndian = true;
        // Set PortProfile::properties
        rtcout.println(rtcout.DEBUG, "setting port.port_type: DataIntPort");
        addProperty("port.port_type", "DataInPort",String.class);
        rtcout.println(rtcout.DEBUG, "setting dataport.data_type: "+data_type);
        addProperty("dataport.data_type", data_type,String.class);
        addProperty("dataport.subscription_type", "Any",String.class);
    }
    
    /**
     *
     * <p> Get properties </p>
     * <p> This method gets properties in the port.  </p>
     * @return Properties Properties
     * 
     */
    public Properties properties() {
        rtcout.println(rtcout.TRACE, "properties()");
    
        return m_properties;
    }

    /**
     *
     * <p> ConnectorId list </p>
     * <p> This operation returns ConnectorId list </p>
     * @return Vector<String> connector list
     * 
     */
    public Vector<String> getConnectorIds() {
        Vector<String> ids = new Vector<String>();
        for (int i=0, len=m_connectors.size(); i < len; ++i) {
            ids.add(m_connectors.elementAt(i).id());
        }
        rtcout.println(rtcout.TRACE, "getConnectorIds(): "+ids);
        return ids;
    }

    /**
     *
     * <p> Connector name list </p>
     * <p> This operation returns Connector name list </p>
     * @return Vector<String> connector name list
     *
     */
    public Vector<String> getConnectorNames() {
        Vector<String> names = new Vector<String>();
        for (int i=0, len=m_connectors.size(); i < len; ++i) {
            names.add(m_connectors.elementAt(i).name());
        }
        rtcout.println(rtcout.TRACE, "getConnectorNames(): "+names);
        return names;
    }

    /** 
     * <p> Getting ConnectorProfile by ID </p>
     * <p> This operation returns Connector specified by ID. </p>
     * @param id Connector ID
     * @return InPortConnector connector
     */
    public InPortConnector getConnectorById(final String id) {
        rtcout.println(rtcout.TRACE, "getConnectorById(id = "+id+")");

        String sid = id;
        for (int i=0, len=m_connectors.size(); i < len; ++i) {
            if (sid.equals(m_connectors.elementAt(i).id())) {
                return m_connectors.elementAt(i);
            }
        }
        rtcout.println(rtcout.WARN, 
                        "ConnectorProfile with the id("+id+") not found.");
        return null;
    }

    /**
     * <p> Getting Connector by name </p>
     * <p> This operation returns Connector specified by name. </p>
     * @param name Connector ID
     * @return InPortConnector connector
     *
     */
    public InPortConnector getConnectorByName(final String name){
        rtcout.println(rtcout.TRACE, "getConnectorByName(name = "+name+")");

        String sname = name;
        for (int i=0, len=m_connectors.size(); i < len; ++i) {
            if (sname.equals(m_connectors.elementAt(i).name())) {
                return m_connectors.elementAt(i);
            }
        }
        rtcout.println(rtcout.WARN, 
                        "ConnectorProfile with the name("+name+") not found.");
        return null;
    }

    /**
     * <p> Getting ConnectorProfile by name </p>
     * <p> This operation returns ConnectorProfile specified by name </p>
     * @param id Connector ID
     * @param prof ConnectorProfileHolder
     * @return boolean false specified ID does not exist
     *
     */
    public boolean getConnectorProfileById(final String id, 
                                    ConnectorBase.ConnectorInfoHolder prof) {
        rtcout.println(rtcout.TRACE, "getConnectorProfileById(id = "+id+")");

        InPortConnector conn = getConnectorById(id);
        if (conn == null) {
            return false;
        }
        prof.value = conn.profile();
        return true;
    }

    /**
     *
     * <p> Getting ConnectorProfile by name </p>
     * <p>This operation returns ConnectorProfile specified by name </p>
     *
     * @param name Connector ID
     * @param prof ConnectorProfile
     * @return boolean false specified name does not exist
     *
     */
    public boolean getConnectorProfileByName(final String name, 
                                      ConnectorBase.ConnectorInfoHolder prof) {
        rtcout.println(rtcout.TRACE, 
                            "getConnectorProfileByName(name = "+name+")");
        InPortConnector conn = getConnectorByName(name);
        if (conn == null) {
            return false;
        }
        prof.value = conn.profile();
        return true;
    }

    /**
     * <p> Initializing properties </p>
     * <p> This method initializes the port in the specified property.  </p>
     * @param prop Property for setting ports
     */
    public void init(Properties prop) {
        rtcout.println(rtcout.TRACE, "init()");

        m_properties.merge(prop);
        if (m_singlebuffer) {
            rtcout.println(rtcout.DEBUG, "single buffer mode.");
            final BufferFactory<RingBuffer<OutputStream>,String> factory 
                    = BufferFactory.instance();
            m_thebuffer = factory.createObject("ring_buffer");

            if (m_thebuffer == null) {
                rtcout.println(rtcout.ERROR, "default buffer creation failed.");
            }
        }
        else {
            rtcout.println(rtcout.DEBUG, "multi buffer mode.");
        }
        initProviders();
        initConsumers();

        int num = -1;
        String limit = m_properties.getProperty("connection_limit","-1");
        try {
            num = Integer.parseInt(limit);
        }
        catch(Exception ex){
            rtcout.println(rtcout.ERROR, 
                    "invalid connection_limit value: "+limit );
        }
        setConnectionLimit(num);
      
    }

    /**
     * {@.ja RTObject_impl::readAll()から呼ばれる仮想関数}
     * {@.en It is a virtual method that is called from 
     *       RTObject_impl::readAll().}
     * <p>
     * {@.ja DataPort からデータを読み出す}
     * {@.en  This method reads out data from DataPort.}
     * </p>
     * @return 
     *   {@.ja true:成功,false:失敗}
     *   {@.en true:Success,false:Failure}
     */
    public abstract boolean read();

    /**
     * <p> Activate all Port interfaces </p>
     *
     * <p> This operation activate all interfaces that is registered in the </p>
     * <p> ports. </p>
     *
     */
    public void activateInterfaces() {
        rtcout.println(rtcout.TRACE, "activateInterfaces()");

        synchronized (m_connectors){
            for (int i=0, len=m_connectors.size(); i < len; ++i) {
                m_connectors.elementAt(i).activate();
                rtcout.println(rtcout.DEBUG, 
                               "activate connector: "
                                    + m_connectors.elementAt(i).name()
                                    +" "
                                    +m_connectors.elementAt(i).id());
            }
        }
    }

    /**
     *
     * <p> Deactivate all Port interfaces </p>
     *
     * <p> This operation deactivate all interfaces that is registered in the </p>
     * <p> ports. </p>
     *
     */
    public void deactivateInterfaces() {
        rtcout.println(rtcout.TRACE, "deactivateInterfaces()");

        synchronized (m_connectors){
            for (int i=0, len=m_connectors.size(); i < len; ++i) {
                m_connectors.elementAt(i).deactivate();
                rtcout.println(rtcout.DEBUG, 
                               "deactivate connector: "
                                    + m_connectors.elementAt(i).name()
                                    +" "
                                    +m_connectors.elementAt(i).id());
            }
        }
    }

    /**
     * <p> Connect the Port </p>
     *
     * <p> This operation establishes connection according to the given
     * ConnectionProfile inforamtion. This function is premised on
     * calling from mainly application program or tools.</p> 
     *
     * <p> To establish the connection among Ports of RT-Components,
     * application programs must call this operation giving
     * ConnectorProfile with valid values as an argument.</p> 
     *
     * <p> Out of ConnectorProfile member variables, "name", "ports"
     * and "properties" members shall be set valid
     * data. "connector_id" shall be set as empty string value or
     * valid string UUID value. </p> 
     *
     * <p> ConnectorProfile::name that is connection identifier shall
     * be any valid CORBA::string. </p> 
     * 
     *
     * <p> ConnectorProfile::connector_id shall be set unique
     * identifier (usually UUID is used) for all connections. Since
     * UUID string value is usually set in the connect() function,
     * caller should just set empty string. If the connect() is called
     * with the same UUID as existing connection, this function
     * returns PRECONDITION_NOT_MET error. However, in order to update
     * the existing connection profile, the "connect()" operation with
     * existing connector ID might be used as valid method by future
     * extension </p> 
     *
     * <p> ConnectorProfile::ports, which is sequence of
     * RTC::PortService references, shall store usually two or more
     * ports' references. As exceptions, the "connect()" operation
     * might be called with only one reference in ConnectorProfile, in
     * case of just getting interfaces information from the port, or
     * connecting a special port (i.e. the peer port except
     * RTC::PortService on CORBA). </p> 
     *
     * <p> ConnectorProfile::properties might be used to give certain
     * properties to the service interfaces associated with the port.
     * The properties is a sequence variable with a pair of key string
     * and Any type value. Although the A variable can store any type
     * of values, it is not recommended except string.</p> 
     *
     * <p> The following is the summary of the ConnectorProfile
     * member to be set when this operation is called.</p> 
     *
     * <ul>
     * <li>- ConnectorProfile::name: The any name of connection</li>
     * <li>- ConnectorProfile::connector_id: Empty string</li>
     * <li>- ConnectorProfile::ports: One or more port references</li>
     * <li>- ConnectorProfile::properties: Properties for the interfaces</li>
     * </ul>
     *
     * <p> connect() operation will call the first port in the
     * sequence of the ConnectorProfile.</p>
     *
     * <p> "noify_connect()"s perform cascaded call to the ports
     * stored in the ConnectorProfile::ports by order. Even if errors
     * are raised by intermediate notify_connect() operation, as long
     * as ports' object references are valid, it is guaranteed that
     * this cascaded call is completed in all the ports.  If invalid
     * or dead ports exist in the port's sequence, the ports are
     * skipped and notify_connect() is called for the next valid port.</p>
     *
     * <p> connect() function returns RTC_OK if all the
     * notify_connect() return RTC_OK. At this time the connection is
     * completed.  If notify_connect()s return except RTC_OK,
     * connect() calls disconnect() operation with the connector_id to
     * destruct the connection, and then it returns error code from
     * notify_connect().</p>
     *
     * <p> The ConnectorProfile argument of the connect() operation
     * returns ConnectorProfile::connector_id and various information
     * about service interfaces that is published by
     * publishInterfaces() in the halfway ports. The connect() and
     * halfway notify_connect() functions never change
     * ConnectorProfile::{name, ports}.</p>
     *
     * @param connector_profile The ConnectorProfile.
     * @return ReturnCode_t The return code of ReturnCode_t type.
     */
    public ReturnCode_t connect(ConnectorProfileHolder connector_profile) {
        //
        NVListHolder nvholder = 
                new NVListHolder(connector_profile.value.properties);
            
        Properties prop = new Properties();
        NVUtil.copyToProperties(prop,nvholder);
        if(null != prop.findNode("dataport")){
            int index = 
                NVUtil.find_index(nvholder,"dataport.serializer.cdr.endian");
            if(index<0){
                CORBA_SeqUtil.push_back(nvholder, 
                    NVUtil.newNVString("dataport.serializer.cdr.endian", 
                                    "little,big"));
                connector_profile.value.properties = nvholder.value;
            }
        }
        return super.connect(connector_profile);
    }
    /**
     * <p> Publish interface information </p>
     *
     * <p> Publish interface information. </p>
     * <p> Assign the Provider information that owned by this port </p>
     * <p> to ConnectorProfile#properties </p>
     *
     * @param cprof The connector profile
     *
     * @return The return code of ReturnCode_t type
     *
     */
    protected ReturnCode_t publishInterfaces(ConnectorProfileHolder cprof) {
        rtcout.println(rtcout.TRACE, "publishInterfaces()");

        ReturnCode_t returnvalue = _publishInterfaces();
        if(returnvalue!=ReturnCode_t.RTC_OK) {
            return returnvalue;
        }

        // prop: [port.outport].
        Properties prop = m_properties;
        {
            Properties conn_prop = new Properties();
            NVListHolder nvlist = new NVListHolder(cprof.value.properties);
            NVUtil.copyToProperties(conn_prop, nvlist);
            prop.merge(conn_prop.getNode("dataport")); //merge ConnectorProfile
        }
        rtcout.println(rtcout.DEBUG, 
                           "ConnectorProfile::properties are as follows.");
        String dumpString = new String();
        dumpString = prop._dump(dumpString, prop, 0);
        rtcout.println(rtcout.DEBUG, dumpString);
                           

        //
       NVListHolder holder = new NVListHolder(cprof.value.properties);
       try{ 
            org.omg.CORBA.Any anyVal = NVUtil.find(holder,
                                           "dataport.serializer.cdr.endian");
            String endian_type;
            if( anyVal.type().kind() == TCKind.tk_wstring ) {
                endian_type = anyVal.extract_wstring();
            } else {
                endian_type = anyVal.extract_string();
            }

            endian_type = StringUtil.normalize(endian_type);
            String[] endian = endian_type.split(",");
            endian_type = "";
            for(int ic=0;ic<endian.length;++ic){
                String str = endian[ic].trim();
                if(str.equals("big") || str.equals("little")){
                    if(endian_type.length()!=0){
                        endian_type = endian_type + ","+ str;
                    }
                    else{
                        endian_type = endian_type + str;
                    }
                }
            }
            int index = NVUtil.find_index(holder, "dataport.serializer.cdr.endian");
            holder.value[index].value.insert_string(endian_type);
            cprof.value.properties = holder.value;
       }
       catch(Exception e){
            ;
       }
        /*
         * Because properties of ConnectorProfileHolder was merged, 
         * the accesses such as prop["dataflow_type"] and 
         * prop["interface_type"] become possible here.
         */
        String dflow_type = prop.getProperty("dataflow_type");
        dflow_type = StringUtil.normalize(dflow_type);

        if (dflow_type.equals("push")) {
            rtcout.println(rtcout.DEBUG, 
                           "dataflow_type = push .... create PushConnector");

            // create InPortProvider
            InPortProvider provider = createProvider(cprof, prop);
            if (provider == null) {
                rtcout.println(rtcout.ERROR, 
                           "InPort provider creation failed.");
                return ReturnCode_t.BAD_PARAMETER;
            }

            // create InPortPushConnector
            InPortConnector connector = createConnector(cprof, prop, provider);
            if (connector == null) {
                rtcout.println(rtcout.ERROR, 
                           "PushConnector creation failed.");
                return ReturnCode_t.RTC_ERROR;
            }
            provider.setConnector(connector);

            rtcout.println(rtcout.DEBUG, 
                           "publishInterface() successfully finished.");
            return ReturnCode_t.RTC_OK;
        }
        else if (dflow_type.equals("pull")) {
            rtcout.println(rtcout.DEBUG, 
                           "dataflow_type = pull .... do nothing.");
            return ReturnCode_t.RTC_OK;
        }

        rtcout.println(rtcout.ERROR, "unsupported dataflow_type:"+dflow_type);
        return ReturnCode_t.BAD_PARAMETER;
    }
    
    /**
     * <p> Subscribe to the interface </p>
     *
     * <p> Subscribe to interface. </p>
     * <p> Derive Provider information that matches Consumer owned by the Port </p>
     * <p> from ConnectorProfile#properties and  </p>
     * <p> set the Consumer to the reference of the CORBA object. </p>
     *
     * @param cprof The connector profile
     *
     * @return ReturnCode_t The return code of ReturnCode_t type
     *
     */
    protected ReturnCode_t subscribeInterfaces(
            final ConnectorProfileHolder cprof) {
        rtcout.println(rtcout.TRACE, "subscribeInterfaces()");

        // prop: [port.outport].
        Properties prop = m_properties;
        {
            Properties conn_prop = new Properties();
            NVListHolder nvlist = new NVListHolder(cprof.value.properties);
            NVUtil.copyToProperties(conn_prop, nvlist);
            prop.merge(conn_prop.getNode("dataport")); //merge ConnectorProfile
        }

        rtcout.println(rtcout.DEBUG, 
                           "ConnectorProfile::properties are as follows.");
        String dumpString = new String();
        dumpString = prop._dump(dumpString, prop, 0);
        rtcout.println(rtcout.DEBUG, dumpString);

        //
       NVListHolder holder = new NVListHolder(cprof.value.properties);
       try{ 
            org.omg.CORBA.Any anyVal = NVUtil.find(holder,
                                           "dataport.serializer.cdr.endian");
            String endian_type;
            if( anyVal.type().kind() == TCKind.tk_wstring ) {
                endian_type = anyVal.extract_wstring();
            } else {
                endian_type = anyVal.extract_string();
            }

            endian_type = StringUtil.normalize(endian_type);
            String[] endian = endian_type.split(",");
            String str = endian[0].trim();
            if(str.length()==0){
                rtcout.println(rtcout.ERROR, "unsupported endian");
                return ReturnCode_t.UNSUPPORTED;
            }
            if(str.equals("little")){
                m_isLittleEndian = true;
            }
            else if(str.equals("big")){
                m_isLittleEndian = false;
            }
            else {
                m_isLittleEndian = true;
            }
       }
       catch(Exception e){
            m_isLittleEndian = true;
       }
        rtcout.println(rtcout.TRACE, "Little Endian = "+m_isLittleEndian);

        /*
         * Because properties of ConnectorProfileHolder was merged, 
         * the accesses such as prop["dataflow_type"] and 
         * prop["interface_type"] become possible here.
         */
        String dflow_type = prop.getProperty("dataflow_type");
        dflow_type = StringUtil.normalize(dflow_type);

        if (dflow_type.equals("push")) {
            rtcout.println(rtcout.DEBUG, 
                           "dataflow_type is push.");
            //
            // setting endian type
            InPortConnector conn = getConnectorById(cprof.value.connector_id);
            if (conn == null) {
                rtcout.println(rtcout.ERROR, 
                    "specified connector not found: "+cprof.value.connector_id);
                return ReturnCode_t.RTC_ERROR;
            }
            conn.setEndian(m_isLittleEndian);

            rtcout.println(rtcout.DEBUG, 
                        "subscribeInterfaces() successfully finished.");
            return ReturnCode_t.RTC_OK;
            
/* zxc
            String id = cprof.value.connector_id;
            synchronized (m_connectors){
                Iterator it = m_connectors.iterator();
                ConnectorBase.ConnectorInfo profile 
                    = new ConnectorBase.ConnectorInfo(cprof.value.name,
                                 cprof.value.connector_id,
                                 CORBA_SeqUtil.refToVstring(cprof.value.ports),
                                 prop); 
                while (it.hasNext()) {
                    InPortConnector connector = (InPortConnector)it.next();
                    if (id.equals(connector.id())) {
                        connector.setEndian(m_isLittleEndian);
                        connector.setListener(profile,m_listeners);
                        return ReturnCode_t.RTC_OK;
                    }
                }
                rtcout.println(rtcout.ERROR, 
                               "specified connector not found: " + id);
                return ReturnCode_t.RTC_ERROR;
            }
*/
        }
        else if (dflow_type.equals("pull")) {
            rtcout.println(rtcout.DEBUG, 
                           "dataflow_type is pull.");

            // create OutPortConsumer
            OutPortConsumer consumer = createConsumer(cprof, prop);
            if (consumer == null) {
                return ReturnCode_t.BAD_PARAMETER;
            }

            // create InPortPullConnector
            InPortConnector connector = createConnector(cprof, prop, consumer);
            if (connector == null) {
                return ReturnCode_t.RTC_ERROR;
            }
            consumer.setConnector(connector);

            connector.setEndian(m_isLittleEndian);
            rtcout.println(rtcout.DEBUG, 
                           "publishInterface() successfully finished.");
            return ReturnCode_t.RTC_OK;
        }

        rtcout.println(rtcout.ERROR, 
                       "unsupported dataflow_type:"+dflow_type);
        return ReturnCode_t.BAD_PARAMETER;
    }
    
    /**
     * <p> Disconnect the interface connection </p>
     *
     * <p> Disconnect the interface connection. </p>
     * <p> Release all objects set in Consumer associated with </p>
     * <p> given ConnectorProfile and unscribe the interface. </p>
     *
     * @param connector_profile The connector profile
     *
     */
    protected void
    unsubscribeInterfaces(final ConnectorProfile connector_profile) {
        rtcout.println(rtcout.TRACE, "unsubscribeInterfaces()");

        String id = connector_profile.connector_id;
        rtcout.println(rtcout.PARANOID, "connector_id: " + id);


        synchronized (m_connectors){
            Iterator it = m_connectors.iterator();
            while (it.hasNext()) {
                InPortConnector connector = (InPortConnector)it.next();
                if (id.equals(connector.id())) {
                    // Connector's dtor must call disconnect()
                    connector.disconnect();
                    it.remove();
                    rtcout.println(rtcout.TRACE, "delete connector: " + id);
                    return;
                }
            }
            rtcout.println(rtcout.ERROR, 
                           "specified connector not found: " + id);
            return;
        }
    }

    /**
     * <p> Adding BufferDataListener type listener </p>
     */
    public void addConnectorDataListener(int type,
                             Observer listener,
                             boolean autoclean) {
        rtcout.println(rtcout.TRACE, "addConnectorDataListener()");
  
        if (type < ConnectorDataListenerType.CONNECTOR_DATA_LISTENER_NUM) {
            m_listeners.connectorData_[type].addObserver(listener);
         }
    }
    public void addConnectorDataListener(int type,Observer listener) {
        this.addConnectorDataListener(type,listener,true);
    }

    /**
     * <p> Removing ConnectorDataListener type listener <p>
     */
    public void removeConnectorDataListener(int type,
                                Observer listener) {
        rtcout.println(rtcout.TRACE, "removeConnectorDataListener()");

        if (type < ConnectorDataListenerType.CONNECTOR_DATA_LISTENER_NUM) {
            m_listeners.connectorData_[type].deleteObserver(listener);
        }
    }
  
    /**
     * <p> Adding ConnectorListener type listener </p>
     */
    public void addConnectorListener(int type,
                                           Observer listener,
                                           boolean autoclean) {
        rtcout.println(rtcout.TRACE,"addConnectorListener()");
  
        if (type < ConnectorListenerType.CONNECTOR_LISTENER_NUM) {
            m_listeners.connector_[type].addObserver(listener);
        }
    }
    
    /**
     * <p> Removing ConnectorListener type listener </p>
     *
     */
    public void removeConnectorListener(int type,
                                              Observer listener) {
        rtcout.println(rtcout.TRACE,"removeConnectorListener()");
  
        if (type < ConnectorListenerType.CONNECTOR_LISTENER_NUM) {
            m_listeners.connector_[type].deleteObserver(listener);
        }
    }
  


    /**
     * <p> InPort provider initialization </p>
     */
    protected void initProviders() {
        rtcout.println(rtcout.TRACE, "initProviders()");

        // create InPort providers
        InPortProviderFactory<InPortProvider,String> factory 
            = InPortProviderFactory.instance();
        Set provider_types = factory.getIdentifiers();
        rtcout.println(rtcout.DEBUG, 
                       "available providers: " + provider_types.toString());

//#ifndef RTC_NO_DATAPORTIF_ACTIVATION_OPTION
        String string_normalize = StringUtil.normalize(m_properties.getProperty("provider_types"));
        if (m_properties.hasKey("provider_types")!=null &&
            !string_normalize.equals("all")) {
            rtcout.println(rtcout.DEBUG, 
                       "allowed providers: " 
                       + m_properties.getProperty("provider_types"));

            Set temp_types = provider_types;
            provider_types.clear();
            Vector<String> active_types 
                = StringUtil.split(m_properties.getProperty("provider_types"), 
                                   ",");

            Set temp_types_set = new HashSet(temp_types);
            Set active_types_set = new HashSet(active_types);
            Iterator it = temp_types_set.iterator();
            while(it.hasNext()) {
                String str = (String)it.next();
                if(active_types_set.contains(str)) {
                    provider_types.add(str);
                }
            }
      }
//#endif
    
        // InPortProvider supports "push" dataflow type
        if (provider_types.size() > 0) {
            rtcout.println(rtcout.DEBUG, 
                           "dataflow_type push is supported");
            appendProperty("dataport.dataflow_type", "push");
            appendProperty("dataport.interface_type",
                           StringUtil.flatten(provider_types));
        }

	Iterator it = provider_types.iterator();
	while(it.hasNext()) {
	    m_providerTypes.add((String)it.next());
	}
    }

    /**
     * <p> OutPort consumer initialization </p>
     */
    protected void initConsumers() {
        rtcout.println(rtcout.TRACE, "iinitConsumers()");

        // create OuPort consumers
        OutPortConsumerFactory<OutPortProvider,String> factory 
            = OutPortConsumerFactory.instance();
        Set consumer_types = factory.getIdentifiers();
        rtcout.println(rtcout.DEBUG, 
                       "available consumer: " + StringUtil.flatten(consumer_types));

//#ifndef RTC_NO_DATAPORTIF_ACTIVATION_OPTION
        String string_normalize = StringUtil.normalize(m_properties.getProperty("consumer_types"));
        if (m_properties.hasKey("consumer_types")!=null &&
            !string_normalize.equals("all")) {
            rtcout.println(rtcout.DEBUG, 
                       "allowed consumers: " 
                       + m_properties.getProperty("consumer_types"));

            Set temp_types = consumer_types;
            consumer_types.clear();
            Vector<String> active_types 
                = StringUtil.split(m_properties.getProperty("consumer_types"), 
                                   ",");

            Set temp_types_set = new HashSet(temp_types);
            Set active_types_set = new HashSet(active_types);
            Iterator it = temp_types_set.iterator();
            while(it.hasNext()) {
                String str = (String)it.next();
                if(active_types_set.contains(str)) {
                    consumer_types.add(str);
                }
            }
        }
//#endif

        // OutPortConsumer supports "pull" dataflow type
        if (consumer_types.size() > 0) {
            rtcout.println(rtcout.DEBUG, 
                           "dataflow_type pull is supported");
            appendProperty("dataport.dataflow_type", "pull");
            appendProperty("dataport.interface_type",
                           StringUtil.flatten(consumer_types));
        }

	Iterator it = consumer_types.iterator();
	while(it.hasNext()) {
	    m_consumerTypes.add((String)it.next());
	}
    }

    /**
     * <p> InPort provider creation </p>
     * <p> InPortProvider is created, </p>
     * <p> and information is published to ConnectorProfile. </p>
     * <p> null is returned if failing in creation. </p>
     */
    protected InPortProvider
    createProvider(ConnectorProfileHolder cprof, Properties prop) {
        if (prop.getProperty("interface_type").length()!=0 &&
            !StringUtil.includes(m_providerTypes, 
                      prop.getProperty("interface_type"),
                      true)) {
            rtcout.println(rtcout.ERROR, "no provider found");
            rtcout.println(rtcout.ERROR, 
                       "interface_type:  "+prop.getProperty("interface_type"));
            rtcout.println(rtcout.ERROR, 
                       "interface_types:  "+m_providerTypes.toString());
            return null;
        }
    
        rtcout.println(rtcout.DEBUG, 
                       "interface_type:  "+prop.getProperty("interface_type"));
        InPortProvider provider;
        InPortProviderFactory<InPortProvider,String> factory 
            = InPortProviderFactory.instance();
        provider = factory.createObject(prop.getProperty("interface_type"));
    
        if (provider != null) {
            rtcout.println(rtcout.DEBUG, "provider created");
            provider.init(prop.getNode("provider"));

            NVListHolder nvlist = new NVListHolder(cprof.value.properties);
            if (!provider.publishInterface(nvlist)) {
                rtcout.println(rtcout.ERROR, 
                               "publishing interface information error");
                factory.deleteObject(provider);
                return null;
            }
	    cprof.value.properties = nvlist.value;
            return provider;
        }

        rtcout.println(rtcout.ERROR, "provider creation failed");
        return null;
    }

    /**
     * <p> InPort provider creation </p>
     * <p> OutPortConsumer is created. </p>
     * <p> null is returned if failing in creation. </p>
     */
    protected OutPortConsumer
    createConsumer(final ConnectorProfileHolder cprof, Properties prop) {
        if (prop.getProperty("interface_type").length()!=0 &&
            !StringUtil.includes(m_consumerTypes, 
                                 prop.getProperty("interface_type"),
                                 true)) {
            rtcout.println(rtcout.ERROR, "no consumer found");
            rtcout.println(rtcout.ERROR, 
                       "interface_type:  "+prop.getProperty("interface_type"));
            rtcout.println(rtcout.ERROR, 
                       "interface_types:  "+m_consumerTypes.toString());
            return null;
        }
    
        rtcout.println(rtcout.DEBUG, 
                       "interface_type:  "+prop.getProperty("interface_type"));
        OutPortConsumer consumer;
        OutPortConsumerFactory<OutPortConsumer,String> factory 
            = OutPortConsumerFactory.instance();
        consumer = factory.createObject(prop.getProperty("interface_type"));
    
        if (consumer != null) {
            rtcout.println(rtcout.DEBUG, "consumer created");
            consumer.init(prop.getNode("consumer"));
    
            NVListHolder nvlist = new NVListHolder(cprof.value.properties);
            if (!consumer.subscribeInterface(nvlist)) {
                rtcout.println(rtcout.ERROR, 
                               "interface subscription failed.");
                factory.deleteObject(consumer);
                return null;
              }

            return consumer;
        }

        rtcout.println(rtcout.ERROR, "consumer creation failed");
        return null;
    }
    /**
     * <p> InPortPushConnector creation </p>
     * <p> Connector is created, </p>
     * <p> preserves it in m_connectors. </p>
     * <p> null is returned if failing in creation. </p>
     */
    protected InPortConnector
    createConnector(ConnectorProfileHolder cprof, Properties prop,
                    InPortProvider provider) {

        ConnectorBase.ConnectorInfo profile 
            = new ConnectorBase.ConnectorInfo(cprof.value.name,
                                 cprof.value.connector_id,
                                 CORBA_SeqUtil.refToVstring(cprof.value.ports),
                                 prop); 
        InPortConnector connector = null;
        synchronized (m_connectors){
            try {
                if (m_singlebuffer) {
                    connector = new InPortPushConnector(profile, provider,
                                                    m_listeners,m_thebuffer);
                }
                else {
                    BufferBase<OutputStream> buffer = null;
                    connector = new InPortPushConnector(profile, provider, 
                                                        m_listeners,buffer);
                }
    
                if (connector == null) {
                    rtcout.println(rtcout.ERROR, 
                                   "old compiler? new returned 0;");
                    return null;
                }
                rtcout.println(rtcout.TRACE, "InPortPushConnector create");
    
                m_connectors.add(connector);
                rtcout.println(rtcout.PARANOID, 
                               "connector push backed: "+m_connectors.size());
                return connector;
            }
            catch (Exception e) {
                rtcout.println(rtcout.ERROR,
                               "InPortPushConnector creation failed");
                return null;
            }
        }
    }
    /**
     * <p> InPortPullConnector creation </p>
     * <p> Connector is created, </p>
     * <p> preserves it in m_connectors. </p>
     * <p> 0 is returned if failing in creation. </p>
     */
    protected InPortConnector
    createConnector(final ConnectorProfileHolder cprof, Properties prop,
                    OutPortConsumer consumer) {
        ConnectorBase.ConnectorInfo profile 
            = new ConnectorBase.ConnectorInfo( cprof.value.name,
                                  cprof.value.connector_id,
                                  CORBA_SeqUtil.refToVstring(cprof.value.ports),
                                  prop); 
        InPortConnector connector = null;
        synchronized (m_connectors){
            try {
                if (m_singlebuffer) {
                    connector = new InPortPullConnector(profile, consumer,
                                                        m_listeners,
                                                        m_thebuffer);
                }
                else {
                    BufferBase<OutputStream> buffer = null;
                    connector = new InPortPullConnector(profile, consumer, 
                                                        m_listeners,
                                                        buffer);
                }

                if (connector == null) {
                    rtcout.println(rtcout.ERROR, 
                                   "old compiler? new returned 0;");
                    return null;
                }
                rtcout.println(rtcout.TRACE, "InPortPushConnector create");

                m_connectors.add(connector);
                rtcout.println(rtcout.PARANOID, 
                               "connector push backed: "+m_connectors.size());
                return connector;
            }
            catch (Exception e) {
                rtcout.println(rtcout.ERROR,
                               "InPortPullConnector creation failed");
                return null;
            }
        }
    }
    /**
     * 
     */
    public boolean isLittleEndian(){
        return m_isLittleEndian;
    }
    protected boolean m_singlebuffer;
    protected BufferBase<OutputStream> m_thebuffer;
    protected Properties m_properties = new Properties();
    protected Vector<String> m_providerTypes = new Vector<String>();
    protected Vector<String> m_consumerTypes = new Vector<String>();
    protected Vector<InPortConnector> m_connectors = new Vector<InPortConnector>();
    private boolean m_isLittleEndian;
    protected ConnectorListeners m_listeners = new ConnectorListeners();
}


