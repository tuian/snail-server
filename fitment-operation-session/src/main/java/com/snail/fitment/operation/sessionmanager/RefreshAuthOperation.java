package com.snail.fitment.operation.sessionmanager;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.type.TypeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.snail.fitment.api.session.IAuthManager;
import com.snail.fitment.api.session.ISessionManager;
import com.snail.fitment.common.code.CommonStatusCode;
import com.snail.fitment.common.constants.OperationConstants;
import com.snail.fitment.model.OperationResult;
import com.snail.fitment.model.SessionInfo;
import com.snail.fitment.model.request.ReturnResult;
import com.snail.fitment.operation.api.OperationContext;
import com.snail.fitment.operation.support.AbstractOperation;

@Lazy(false)
@Component("refreshAuthOperation")
public class RefreshAuthOperation extends AbstractOperation {

	@Autowired
	private IAuthManager defaultAuthManager;

	@Autowired
	private ISessionManager sessionManager;

	public RefreshAuthOperation() {
		super.initName("RefreshAuth", OperationConstants.TYPE_REQ, OperationConstants.OP_SESSION_REFRESH_AUTH);

		_paramDefine.put("authToken", TypeFactory.fastSimpleType(String.class));
		_paramDefine.put("appKey", TypeFactory.fastSimpleType(String.class));
	}

	@Override
	protected OperationResult doExecute(OperationContext context, OperationResult result) throws Exception {
		Map<String, Object> paramList = context.getReqParams();
		String authToken = getParamValue(paramList, "authToken", "");
		String appKey = getParamValue(paramList, "appKey", "");


		Map<String, String> tokens = new HashMap<String, String>();
		tokens.put("authToken", authToken);
		tokens.put("appKey", appKey);
		
		SessionInfo info = defaultAuthManager.refreshWithToken(defaultAuthManager.createToken(tokens));

		if (info != null) {
			sessionManager.updateSession(info);

			ReturnResult<String> returnResult = ReturnResult.getSuccessInstance();
			returnResult.setReturnContent(info.getSessionId());

			result.getResultList().put("stateCode", returnResult.getCode());
			result.getResultList().put("stateDesc", returnResult.getDesc());
			result.getResultList().put("sessionId", returnResult.getReturnContent());
			result.getResultList().put("session", info);
		} else {
			result.getResultList().put("stateCode", CommonStatusCode.COMMON_SERVER_ERROR);
			result.getResultList().put("stateDesc", "认证失败");
			return result;
		}
		return result;
	}

}
