package jp.go.aist.rtm.rtclink.model.component;

import jp.go.aist.rtm.rtclink.model.core.CorbaWrapperObject;

import org.eclipse.emf.common.util.EList;

/**
 * 接続先を表現するクラス
 * @model
 */
public interface ConnectorTarget extends CorbaWrapperObject{
	
	/**
	 * <!-- begin-user-doc --> EList<? extends Connector> <!-- end-user-doc
	 * -->
	 * 
	 * @model type="Connector" opposite="target"
	 */
	public EList getTargetConnectors();

	/**
	 * @model
	 */
	public boolean validateConnector(ConnectorSource source);

	public static final String TARGET_CONNECTION = "TARGET_CONNECTION";

}

