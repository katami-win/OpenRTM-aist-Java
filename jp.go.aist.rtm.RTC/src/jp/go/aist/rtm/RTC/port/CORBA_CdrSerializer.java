package jp.go.aist.rtm.RTC.port;

import java.lang.reflect.Field;

import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.Streamable;

import jp.go.aist.rtm.RTC.ObjectCreator;
import jp.go.aist.rtm.RTC.ObjectDestructor;
import jp.go.aist.rtm.RTC.SerializerFactory;
import jp.go.aist.rtm.RTC.util.DataRef;
import jp.go.aist.rtm.RTC.util.Properties;
/**
 * {@.ja CORBAのCDRシリアライザの実装}
 * {@.en Implementation of CORBA CDR Serializer}
 * <p>
 */
public class CORBA_CdrSerializer implements ByteDataStreamBase, ByteDataStream, ObjectCreator<CORBA_CdrSerializer>, ObjectDestructor {
    /**
     * {@.ja コンストラクタ}
     * {@.en Constructor}
     * <p>
     *
     */
    public CORBA_CdrSerializer(){

    }
    /**
     * {@.ja 初期化関数(未使用)}
     * {@.en Initialization function (not used)}
     *
     * <p>
     * </p>
     * @param  prop
     *   {@.ja プロパティ(コネクタプロファイルから取得)}
     *   {@.en Properties (getted from connector profile)}
     *
     */
    public void init(final Properties prop)  {
    }
    /**
     * {@.ja 保持しているバッファにデータを書き込む}
     * {@.en writes data to a buffer that is held.}
     *
     * <p>
     * </p>
     * @param  buffer
     *   {@.ja 書き込み元のバッファ}
     *   {@.en Source buffer}
     * @param length データのサイズ
     *   {@.ja データのサイズ}
     *   {@.en Data size}
     *
     */
    public void writeData(final byte[]  buffer, long length) {
//            m_cdr.writeCdrData(buffer, length);
    }
    /**
     * {@.ja 引数のバッファにデータを書き込む}
     * {@.en  writes data to the argument buffer.}
     *
     * <p>
     * </p>
     * @param  buffer
     *   {@.ja 書き込み先のバッファ}
     *   {@.en Destination buffer}
     * @param length データのサイズ
     *   {@.ja データのサイズ}
     *   {@.en Data size}
     *
     */
    public final void readData(byte[] buffer, long length) {
//            m_cdr.readCdrData(buffer, length);
    }
    /**
     * {@.ja データの長さを取得}
     * {@.en gets the length of the data.}
     *
     * @return 
     *   {@.ja データの長さ}
     *   {@.en data length}
     */

    public final long getDataLength() {
//            return m_cdr.getCdrDataLength();
            return 0;
    }
    /**
     * {@.ja データの符号化}
     * {@.en Data serializing}
     *
     * @param data
     *   {@.ja 符号化前のデータ}
     *   {@.en The data to serialize}
     * @return 
     *   {@.ja True:成功 False:失敗}
     *   {@.en True:success False:failure}
     *
     */
    public <DataType> boolean serialize(final DataType data) {
            //return m_cdr.serializeCDR(data);
            return true;
    }
    public <DataType> SerializeReturnCode serialize(final DataType data, OutputStream cdr) {
        Class cl = data.getClass();
        String str = cl.getName();
        try {
            Class holder = Class.forName(str+"Holder",
                                         true,
                                         this.getClass().getClassLoader());
            m_streamable = (Streamable)holder.newInstance();
            m_field = m_streamable.getClass().getField("value");
        }
        catch(NoSuchFieldException e){
            //getField throws
            return SerializeReturnCode.SERIALIZE_ERROR;
        }
        catch(java.lang.InstantiationException e){
            return SerializeReturnCode.SERIALIZE_ERROR;
        }
        catch(ClassNotFoundException e){
            //forName throws
            return SerializeReturnCode.SERIALIZE_ERROR;
        }
        catch(IllegalAccessException e){
            //set throws
            return SerializeReturnCode.SERIALIZE_ERROR;
        }
        catch(IllegalArgumentException e){
            //invoke throws
            return SerializeReturnCode.SERIALIZE_ERROR;
        }
        try {
            m_field.set(m_streamable,data);
            m_streamable._write(cdr);
            return SerializeReturnCode.SERIALIZE_OK;

        }
        catch(IllegalAccessException e){
            return SerializeReturnCode.SERIALIZE_ERROR;
        }
        catch(IllegalArgumentException e){
            return SerializeReturnCode.SERIALIZE_ERROR;
        }
    }

    /**
     * {@.ja データの復号化}
     * {@.en Data deserializing}
     *
     * <p>
     * </p>
     * @param data
     *   {@.ja 復号前のデータ}
     *   {@.en The data to deserialize}
     * @return 
     *   {@.ja True:成功 False:失敗}
     *   {@.en True:success False:failure}
     *
     */
    public <DataType> boolean deserialize(DataRef<DataType> data) {
    //public <DataType> SerializeReturnCode deserialize(DataType data){
            //return m_cdr.deserializeCDR(data);
            return true;
    }
    public <DataType> SerializeReturnCode deserialize(DataRef<DataType> data,OutputStream cdr) {
        Class cl = data.v.getClass();
        String str = cl.getName();
        if(str.indexOf("OutputStream")!=-1){
            data.v =  (DataType)cdr;
            return SerializeReturnCode.SERIALIZE_OK;
        }
        if(str.indexOf("InputStream")!=-1){
            data.v =  (DataType)cdr.create_input_stream();
            return SerializeReturnCode.SERIALIZE_OK;
        }
        InputStream in_cdr = cdr.create_input_stream();
        try {
            Class holder = Class.forName(str+"Holder",
                                         true,
                                         this.getClass().getClassLoader());
            m_streamable = (Streamable)holder.newInstance();
            m_field = m_streamable.getClass().getField("value");
        }
        catch(NoSuchFieldException e){
            //getField throws
            return SerializeReturnCode.SERIALIZE_ERROR;
        }
        catch(java.lang.InstantiationException e){
            return SerializeReturnCode.SERIALIZE_ERROR;
        }
        catch(ClassNotFoundException e){
            //forName throws
            return SerializeReturnCode.SERIALIZE_ERROR;
        }
        catch(IllegalAccessException e){
            //set throws
            return SerializeReturnCode.SERIALIZE_ERROR;
        }
        catch(IllegalArgumentException e){
            //invoke throws
            return SerializeReturnCode.SERIALIZE_ERROR;
        }
        try {
            m_streamable._read(in_cdr);
            data.v = (DataType)m_field.get(m_streamable);
            return SerializeReturnCode.SERIALIZE_OK;
        }
        catch(IllegalAccessException e){
            return SerializeReturnCode.SERIALIZE_ERROR;
        }
        catch(IllegalArgumentException e){
            return SerializeReturnCode.SERIALIZE_ERROR;
        }
    }
    /**
     * {@.ja コピーコンストラクタ}
     * {@.en copy container}
     * <p>
     *
     * @param rhs
     *   {@.ja CORBA_CdrSerializer}
     *   {@.en CORBA_CdrSerializer object}
     */
    public <DataType> CORBA_CdrSerializer(final CORBA_CdrSerializer rhs) {
            m_cdr = rhs.m_cdr;
    }
    /**
     * {@.ja エンディアンを設定する}
     * {@.en Sets an endian.}
     *
     * @param little_endian
     *   {@.ja true: little, false: big}
     *   {@.en true: little, false: big}
     *
     */
    public void isLittleEndian(boolean little_endian) {
            //m_cdr.setEndian(little_endian);
    }
    /**
     * {@.ja InPortCorbaCdrProvider を生成する}
     * {@.en Creats InPortCorbaCdrProvider}
     * 
     * @return 
     *   {@.ja 生成されたInPortProvider}
     *   {@.en Object Created instances}
     *
     */
    public CORBA_CdrSerializer creator_() {
        return new CORBA_CdrSerializer();
    }
    /**
     * {@.ja InPortCorbaCdrProvider を破棄する}
     * {@.en Destructs InPortCorbaCdrProvider}
     * 
     * @param obj
     *   {@.ja 破棄するインタスタンス}
     *   {@.en The target instances for destruction}
     *
     */
    public void destructor_(Object obj) {
/*
        try{
            byte[] oid 
                = _default_POA().servant_to_id((CORBA_CdrSerializer)obj);
            _default_POA().deactivate_object(oid);
        }
        catch(Exception e){
            e.printStackTrace();
        }
*/	
        obj = null;
    }

    public static void CORBA_CdrSerializerInit() {
        final SerializerFactory<CORBA_CdrSerializer,String> factory 
            = SerializerFactory.instance();
        factory.addFactory("corba",
                    new CORBA_CdrSerializer(),
                    new CORBA_CdrSerializer());
    
    }


    private Streamable m_streamable = null;
    private Field m_field = null;
    protected OutputStream m_cdr;
    protected boolean m_endian;

}
