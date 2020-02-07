package jp.go.aist.rtm.RTC.port;

import java.util.Vector;

import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.InputStream;

import jp.go.aist.rtm.RTC.buffer.BufferBase;
import jp.go.aist.rtm.RTC.buffer.NullBuffer;
import jp.go.aist.rtm.RTC.util.DataRef;
import jp.go.aist.rtm.RTC.util.DoubleHolder;
import jp.go.aist.rtm.RTC.util.Properties;
import junit.framework.TestCase;
import RTC.TimedDouble;
import RTC.TimedDoubleHolder;

/**
 * <p>InPortクラスのためのテストケースです。</p>
 */
public class InPortTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();

        this.m_Double_val = new TimedDouble();
        this.m_value = new DataRef<TimedDouble>(m_Double_val);
        this.m_pInport = new InPort<TimedDouble>(
                 "double", this.m_value);
        this.m_pInport.setOnWrite(new OnWriteMock());
    }

    protected void tearDown() throws Exception {
        super.tearDown();

        this.m_pInport = null;
    }

    private InPort<TimedDouble> m_pInport;
    private DataRef<TimedDouble> m_value;
    private TimedDouble m_Double_val;

    public class FullBuffer<DataType> implements BufferBase<DataType> {
        public void init(final Properties prop){
        }
        public jp.go.aist.rtm.RTC.buffer.ReturnCode reset(){
            return jp.go.aist.rtm.RTC.buffer.ReturnCode.BUFFER_OK;
        }
        public DataType wptr(int n){
            return m_buffer;
        }
        public DataType wptr(){
            return m_buffer;
        }
        public jp.go.aist.rtm.RTC.buffer.ReturnCode advanceWptr(int n){
            return jp.go.aist.rtm.RTC.buffer.ReturnCode.BUFFER_OK;
        }
        public jp.go.aist.rtm.RTC.buffer.ReturnCode advanceWptr(){
            return jp.go.aist.rtm.RTC.buffer.ReturnCode.BUFFER_OK;
        }
        public int writable(){
            return 1;
        }
        public boolean full(){
            return true;
        }
        public DataType rptr(int n){
            return m_buffer;
        }
        public DataType rptr(){
            return m_buffer;
        }
        public jp.go.aist.rtm.RTC.buffer.ReturnCode advanceRptr(){
            return jp.go.aist.rtm.RTC.buffer.ReturnCode.BUFFER_OK;
        }
        public jp.go.aist.rtm.RTC.buffer.ReturnCode advanceRptr(int n){
            return jp.go.aist.rtm.RTC.buffer.ReturnCode.BUFFER_OK;
        }
        public int readable(){
            return 1;
        }
        public boolean empty(){
            return true;
        }
        public int length(){
            return 1;
        }
        public jp.go.aist.rtm.RTC.buffer.ReturnCode length(int n){
            return jp.go.aist.rtm.RTC.buffer.ReturnCode.BUFFER_OK;
        }
        public jp.go.aist.rtm.RTC.buffer.ReturnCode write(final DataType value){
            return jp.go.aist.rtm.RTC.buffer.ReturnCode.BUFFER_OK;
        }
        public jp.go.aist.rtm.RTC.buffer.ReturnCode write(final DataType value,
                            int sec, int nsec){
            return jp.go.aist.rtm.RTC.buffer.ReturnCode.BUFFER_OK;
        }
        public jp.go.aist.rtm.RTC.buffer.ReturnCode read(DataRef<DataType> valueRef){
            return jp.go.aist.rtm.RTC.buffer.ReturnCode.BUFFER_OK;
        }
        public jp.go.aist.rtm.RTC.buffer.ReturnCode read(DataRef<DataType> valueRef, int sec, int nsec){
            return jp.go.aist.rtm.RTC.buffer.ReturnCode.BUFFER_OK;
        }
        public jp.go.aist.rtm.RTC.buffer.ReturnCode put(final DataType data){
            return jp.go.aist.rtm.RTC.buffer.ReturnCode.BUFFER_OK;
        }
        public DataType get(){
            return m_buffer;
        }
        public jp.go.aist.rtm.RTC.buffer.ReturnCode get(DataRef<DataType> value){
            return jp.go.aist.rtm.RTC.buffer.ReturnCode.BUFFER_OK;
        }
        private DataType m_buffer;
        public boolean isFull() {
            return true;
        }
    }

    class OnWriteMock<DataType> implements OnWrite<DataType> {
        public void run(DataType value) {
            m_value = value;
        }
        DataType m_value;
    }

    class OnOverflowMock<DataType> implements OnOverflow<DataType> {
        public void run(DataType value) {
            m_value = value;
        }
        DataType m_value;
    }

    class OnWriteConvertMock implements OnWriteConvert<TimedDouble> {
        public OnWriteConvertMock(double amplitude) {
            m_amplitude = amplitude;
        }
        public TimedDouble run(TimedDouble value) {
            return new TimedDouble(new RTC.Time(0,0),m_amplitude * value.data);
        }
        double m_amplitude;
    }
    class MockInPortConnector extends InPortConnector {
        public MockInPortConnector(ConnectorInfo profile, 
                    ConnectorListeners listeners,
                    BufferBase<OutputStream> buffer) {
            super(profile, listeners, buffer);
        }
        public void setListener(ConnectorInfo profile, 
                        ConnectorListeners listeners){
        }
        public ReturnCode disconnect() {
            return ReturnCode.PORT_OK;
        }
        public void deactivate(){}; // do nothing
        public  void activate(){}; // do nothing
        public ReturnCode read(DataRef<InputStream> data) {
    
            org.omg.CORBA.Any any = m_orb.create_any(); 
            OutputStream cdr = any.create_output_stream();
            TimedDouble ddata = new TimedDouble(); 
            ddata.tm = new RTC.Time(0,0);
            ddata.data = _data; 
            TimedDoubleHolder holder = new TimedDoubleHolder();
            holder.value = ddata;
            holder._write(cdr); 
            data.v = cdr.create_input_stream();
            return ReturnCode.PORT_OK;
        }
        public void write_test_data(double data) {
            _data = data;
        }
        protected double _data = 0.0;
    }
    /**
     * <p>write()メソッドとread()メソッドのテスト
     * <ul>
     * <li>write()で書き込んだ値が、read()で正しく読み出されるか？</li>
     * </ul>
     * </p>
     */
    public void test_write_and_read() {
        Vector<InPortConnector> cons = m_pInport.connectors();
        MockInPortConnector inport_conn = new MockInPortConnector(null,null,null);
        cons.add(inport_conn);
        for (int i = 0; i < 100; i++) {
            double writeValue = i * 3.14159265;
            // 正常にデータ書き込みを行えることを確認する
            inport_conn.write_test_data(writeValue);
            // write()で書き込んだ値が、read()で正しく読み出されるか？
            TimedDouble readValue = this.m_pInport.extract();
            assertEquals(writeValue, readValue.data);
        }
    }
    /**
     * <p>ポート名取得のテスト
     * <ul>
     * <li>name()メソッドにより正しくポート名を取得できるか？</li>
     * </ul>
     * </p>
     */
    public void test_name() {
        assertEquals("double", this.m_pInport.name());
    }
    
    /**
     * <p>write()メソッドとread()メソッドのテスト
     * <ul>
     * <li>write()で書き込んだ値が、read()で正しく読み出されるか？</li>
     * </ul>
     * </p>
     */
    public void test_write() {
        Vector<InPortConnector> cons = m_pInport.connectors();
        MockInPortConnector inport_conn = new MockInPortConnector(null,null,null);
        cons.add(inport_conn);
        
        for (int i = 0; i < 100; i++) {
            double writeValue = i * 1.0;
            // 正常にデータ書き込みを行えることを確認する
            inport_conn.write_test_data(writeValue);
            
            // データ読み込みを行い、OnWriteConvertコールバックによりフィルタされた結果が取得できることを確認する
            this.m_pInport.read();
            assertEquals(new Double(i), this.m_value.v.data);
        }
    }
    /**
     * <p>バッファフルでない時の、write()メソッドのOnOverflowコールバック呼出テスト
     * <ul>
     * <li>バッファフルでない場合、OnOverflowコールバックが意図どおり未呼出のままか？</li>
     * </ul>
     * </p>
     */
    public void test_write_OnOverflow_not_full() {
        OnOverflowMock<TimedDouble> onOverflow = new OnOverflowMock<TimedDouble>();
        onOverflow.m_value = new TimedDouble(new RTC.Time(0,0),0.0);;
        this.m_pInport.setOnOverflow(onOverflow);

        // write()メソッドは成功するか？
        TimedDouble value = new TimedDouble(new RTC.Time(0,0),3.14159265);
        DataRef<TimedDouble> writeValue = new DataRef<TimedDouble>(value);
        this.m_pInport.write(writeValue);
        
        // バッファフルでない場合、OnOverflowコールバックが意図どおり未呼出のままか？
        assertEquals((double) 0.0, onOverflow.m_value.data);
        
    }
    /**
     * <p>update()メソッドにより、書き込んだデータがバインド変数に正しく反映されることをテストします。</p>
     */
    public void test_binding() {

        for (int i = 0; i < 100; i++) {
            TimedDouble value = new TimedDouble(new RTC.Time(0,0),i*1.0);
            DataRef<TimedDouble> writeValue = new DataRef<TimedDouble>(value);
            this.m_pInport.write(writeValue);
            this.m_pInport.update();
            assertEquals(i*1.0,this.m_value.v.data);
        }
    }
}
