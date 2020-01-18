package org.truenewx.tnxjeex.fss.service;

import org.truenewx.tnxjeex.fss.service.model.FssProvider;

/**
 * 文件存储服务提供商访问器
 *
 * @author jianglei
 */
public interface FssProviderAccessor extends FssAccessor {

    FssProvider getProvider();

}
