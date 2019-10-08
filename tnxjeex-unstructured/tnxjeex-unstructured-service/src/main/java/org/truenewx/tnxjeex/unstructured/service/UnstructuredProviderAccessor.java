package org.truenewx.tnxjeex.unstructured.service;

import org.truenewx.tnxjeex.unstructured.service.model.UnstructuredProvider;

/**
 * 非结构化存储服务提供商访问器
 *
 * @author jianglei
 */
public interface UnstructuredProviderAccessor extends UnstructuredAccessor {

    UnstructuredProvider getProvider();

}
