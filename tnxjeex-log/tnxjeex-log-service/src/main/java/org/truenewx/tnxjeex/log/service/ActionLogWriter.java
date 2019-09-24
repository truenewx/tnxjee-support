package org.truenewx.tnxjeex.log.service;

import java.io.Serializable;

import org.truenewx.tnxjeex.log.model.Action;

/**
 * 操作日志记录器
 *
 * @author jianglei
 */
public interface ActionLogWriter<K extends Serializable> {

    void add(K userId, Action action);

}
