package jp.go.aist.rtm.RTC.port;

import junit.framework.TestCase;

import java.util.Observable;
import java.util.Vector;

import org.omg.CORBA.ORB;
import org.omg.CORBA.portable.OutputStream;

import RTC.TimedLong;
import RTC.TimedLongHolder;

import jp.go.aist.rtm.RTC.port.ConnectorBase;
import jp.go.aist.rtm.RTC.util.DataRef;
import jp.go.aist.rtm.RTC.util.Properties;

public class ConnectorDataListenerTTests extends TestCase {

    class TestListener extends ConnectorDataListenerT<TimedLong>{
        public TestListener(){
            super(TimedLong.class);
            m_data = 0;
        }
        public jp.go.aist.rtm.RTC.connectorListener.ReturnCode 
            operator(ConnectorBase.ConnectorInfo arg,
                               TimedLong data) {
            m_data = data.data;
            return jp.go.aist.rtm.RTC.connectorListener.ReturnCode.NO_CHANGE;
        }
        public int getOpeData(){
            return m_data;
        }
        private int m_data;
    }
    class TestObservable extends Observable {
        public void notify(ConnectorBase.ConnectorInfo info,
                DataRef<OutputStream> cdrdata) {
            super.setChanged();
            jp.go.aist.rtm.RTC.connectorListener.ReturnCode ret = 
		    jp.go.aist.rtm.RTC.connectorListener.ReturnCode.NO_CHANGE;
            ConnectorDataListenerArgumentDataRef<OutputStream> arg 
                = new ConnectorDataListenerArgumentDataRef<OutputStream>(info,cdrdata.v);
            super.notifyObservers((Object)arg);
            super.clearChanged();
            ret = arg.getReturnCode();
        }
    }
    protected void setUp() throws Exception {
        super.setUp();
    }
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    public void test_update() {
        TestObservable obs = new TestObservable ();
        TestListener listener = new TestListener();
        obs.addObserver(listener);
	Vector<String> ports =  new Vector<String>(); 
        Properties prop = new Properties();
        ConnectorBase.ConnectorInfo info
            = new ConnectorBase.ConnectorInfo(
                                 "testname",
                                 "id1",
                                 ports,
                                 prop);

	java.util.Properties props = new java.util.Properties();
        props.put("org.omg.CORBA.ORBInitialPort", "2809");
        props.put("org.omg.CORBA.ORBInitialHost", "localhost");
        ORB orb = ORB.init(new String[0], props);
        org.omg.CORBA.Any any = orb.create_any(); 

        OutputStream cdr = any.create_output_stream();
        TimedLong data = new TimedLong(new RTC.Time(0,0),12345);
        TimedLongHolder holder = new TimedLongHolder(data);
        holder._write(cdr);
        DataRef<OutputStream> dataref = new DataRef<OutputStream>(cdr);
        obs.notify(info,dataref);
        assertEquals(12345, listener.getOpeData());
    }
}

