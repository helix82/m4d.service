package su.opencode.digsigcik.synapi.crypto;

import su.opencode.digsigcik.synapi.dscp.Container;
import su.opencode.digsigcik.synapi.dscp.DscpContainerException;
import su.opencode.digsigcik.synapi.dscp.ResultFileDescription;
import su.opencode.digsigcik.synapi.dscp.ResultTypeEnum;

public interface BasicSecurityProvider extends SecurityProvider {
	ResultFileDescription provideSecurityAction(Container container, ResultTypeEnum resultType, SecurityProvider provider) throws DscpContainerException;
}
