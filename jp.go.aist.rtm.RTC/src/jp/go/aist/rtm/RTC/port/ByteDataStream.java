package jp.go.aist.rtm.RTC.port;

import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;

import jp.go.aist.rtm.RTC.util.DataRef;
/**
 * {@.ja シリアライザのテンプレートクラス}
 * {@.en Serializer template class}
 *<p>
 *{@.ja シリアライザを実装する場合は必ずこのクラスを継承する必要がある
 * Factory にシリアライザを登録すると使用可能
 * 使用するデータ型全てに対してファクトリに登録する必要がある}
 *{@.en This class must be inherited when implementing a serializer.
 * Available after registering a serializer with Factory.
 * All data types used must be registered with the factory.}
 *
 */
public interface ByteDataStream {
    
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
    <DataType> SerializeReturnCode serialize(final DataType data, OutputStream cdr);
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
    <DataType> SerializeReturnCode deserialize(DataRef<DataType> data,OutputStream cdr);
}

