package jp.go.aist.rtm.RTC.port;

import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;

import jp.go.aist.rtm.RTC.PublisherBaseFactory;
import jp.go.aist.rtm.RTC.port.publisher.PublisherBase;
import jp.go.aist.rtm.RTC.port.publisher.PublisherFactory;
import jp.go.aist.rtm.RTC.port.publisher.PublisherFlush;
import jp.go.aist.rtm.RTC.port.publisher.PublisherNew;
import jp.go.aist.rtm.RTC.port.publisher.PublisherPeriodic;
import jp.go.aist.rtm.RTC.util.Properties;
import junit.framework.TestCase;
import _SDOPackage.NVListHolder;

/**
 * <p>PublisherFactoryクラスのためのテストケースです。</p>
 */
public class PublisherFactoryTests extends TestCase {

    class NullConsumer implements InPortConsumer {
        public NullConsumer() {
            super();
        }
        public void init(Properties prop) {
        }
        public void push() {
        }
        public InPortConsumer clone() {
            return new NullConsumer();
        }
        public boolean subscribeInterface(NVListHolder holder) {
            return true;
        }
        public void unsubscribeInterface(NVListHolder holder) {
        }
        public ReturnCode put(final OutputStream data) {
            return ReturnCode.PORT_OK;
        }
        public void publishInterfaceProfile(NVListHolder properties) {
        }
        public void setConnector(OutPortConnector connector) {
        }
    };

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * <p>create()メソッドのテスト
     * <ul>
     * <li>"New"を指定した場合、PublisherNewが生成されるか？</li>
     * <li>"Periodic"を指定した場合、PublisherPeriodicが生成されるか？</li>
     * <li>"Flush"を指定した場合、PublisherFlushが生成されるか？</li>
     * </ul>
     * </p>
     */
    public void test_create() {
        //PublisherFactory factory = new PublisherFactory();
        PublisherBaseFactory<PublisherBase,String> factory
                = PublisherBaseFactory.instance();
        factory.addFactory("new",
                    new PublisherNew(),
                    new PublisherNew());
        factory.addFactory("periodic",
                    new PublisherPeriodic(),
                    new PublisherPeriodic());
        factory.addFactory("flush",
                    new PublisherFlush(),
                    new PublisherFlush());
        
        // "New"を指定した場合、PublisherNewが生成されるか？
        PublisherBase publisherNew = factory.createObject("new");
        assertEquals( publisherNew.getName(), "new");
        // "Periodic"を指定した場合、PublisherPeriodicが生成されるか？
        PublisherBase publisherPeriodic = factory.createObject("periodic");
        assertEquals( publisherPeriodic.getName(), "periodic");
        // "Flush"を指定した場合、PublisherFlushが生成されるか？
        PublisherBase publisherFlush = factory.createObject("flush");
        assertEquals( publisherFlush.getName(), "flush");
    }
}
