package jp.go.aist.rtm.RTC;

/**
 * {@.ja InPortConsumer用ファクトリの実装}
 * {@.en Implement of factory for InPortConsumer} 
 */
public class InPortConsumerFactory<ABSTRACTCLASS,IDENTIFIER> extends FactoryGlobal<ABSTRACTCLASS,IDENTIFIER> {

    /**
     * {@.ja コンストラクタ。}
     * {@.en Constructor}
     */
    private InPortConsumerFactory() {

    }
    /**
     * {@.ja インスタンス生成。}
     * {@.en Create instance}
     *
     * <p>
     * {@.ja インスタンスを生成する。}
     *
     * @return 
     *   {@.ja インスタンス}
     *   {@.en InPortConsumerFactory object}
     *
     */
    synchronized public static InPortConsumerFactory instance() {
        if (factory_global == null) {
            try {
                factory_global = new InPortConsumerFactory();
            } catch (Exception e) {
                factory_global = null;
            }
        }

        return factory_global;
    }
    /**
     *  <p> object </p>
     */
    private static InPortConsumerFactory factory_global;
}

