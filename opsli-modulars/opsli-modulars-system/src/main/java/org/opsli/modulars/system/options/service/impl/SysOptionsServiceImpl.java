/**
* Copyright 2020 OPSLI 快速开发平台 https://www.opsli.com
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License. You may obtain a copy of
* the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
* License for the specific language governing permissions and limitations under
* the License.
*/

package org.opsli.modulars.system.options.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.opsli.api.wrapper.system.options.OptionsModel;
import org.opsli.common.constants.MyBatisConstants;
import org.opsli.common.exception.ServiceException;
import org.opsli.core.base.service.impl.CrudServiceImpl;
import org.opsli.core.msg.CoreMsg;
import org.opsli.core.utils.OptionsUtil;
import org.opsli.modulars.system.SystemMsg;
import org.opsli.modulars.system.options.entity.SysOptions;
import org.opsli.modulars.system.options.mapper.SysOptionsMapper;
import org.opsli.modulars.system.options.service.ISysOptionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
* @BelongsProject: opsli-boot

* @BelongsPackage: org.opsli.modulars.system.options.service.impl

* @Author: Parker
* @CreateTime: 2021-02-07 18:24:38
* @Description: 系统参数 Service Impl
*/
@Service
public class SysOptionsServiceImpl extends CrudServiceImpl<SysOptionsMapper, SysOptions, OptionsModel>
    implements ISysOptionsService {

    @Autowired(required = false)
    private SysOptionsMapper mapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OptionsModel insert(OptionsModel model) {
        if(model == null){
            return null;
        }

        // 唯一验证
        Integer count = this.uniqueVerificationByCode(model);
        if(count != null && count > 0){
            // 重复
            throw new ServiceException(SystemMsg.EXCEPTION_OPTIONS_UNIQUE);
        }

        return super.insert(model);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public OptionsModel update(OptionsModel model) {
        if(model == null){
            return null;
        }

        // 唯一验证
        Integer count = this.uniqueVerificationByCode(model);
        if(count != null && count > 0){
            // 重复
            throw new ServiceException(SystemMsg.EXCEPTION_OPTIONS_UNIQUE);
        }

        model = super.update(model);
        if(model != null){
            // 清除缓存
            this.clearCache(Collections.singletonList(model));
        }

        return model;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateOptions(Map<String, String> params) {
        if(CollUtil.isEmpty(params)){
            // 更新异常
            throw new ServiceException(SystemMsg.EXCEPTION_OPTIONS_UPDATE);
        }

        List<String> optionsCode = Lists.newArrayList();
        optionsCode.addAll(params.keySet());

        // 获得所有的原始内容
        QueryWrapper<SysOptions> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("option_code", optionsCode);
        List<SysOptions> optionsList = this.findList(queryWrapper);
        Map<String, OptionsModel> resourceDataDict = Maps.newHashMap();
        for (SysOptions option : optionsList) {
            resourceDataDict.put(option.getOptionCode(),
                    transformT2M(option)
                    );
        }

        // 循环修改
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            OptionsModel optionsModel = resourceDataDict.get(key);
            if(optionsModel == null){
                // 更新异常
                throw new ServiceException(SystemMsg.EXCEPTION_OPTIONS_UPDATE);
            }

            // 设置值
            optionsModel.setOptionValue(value);
            optionsModel.setVersion(null);

            // 更新
            this.update(optionsModel);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(String id) {
        OptionsModel model = super.get(id);
        boolean ret = super.delete(id);

        if(ret){
            // 清除缓存
            this.clearCache(Collections.singletonList(model));
        }
        return ret;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAll(String[] ids) {
        QueryWrapper<SysOptions> queryWrapper = new QueryWrapper<>();
        queryWrapper.in(MyBatisConstants.FIELD_ID, Convert.toList(String.class, ids));
        List<OptionsModel> modelList = super.transformTs2Ms(
                super.findList(queryWrapper)
        );

        // 清除缓存
        this.clearCache(modelList);

        return super.deleteAll(ids);
    }

    // =======================

    /**
     * 唯一验证
     * @param model model
     * @return Integer
     */
    @Transactional(readOnly = true)
    public Integer uniqueVerificationByCode(OptionsModel model){
        if(model == null){
            return null;
        }
        QueryWrapper<SysOptions> wrapper = new QueryWrapper<>();
        wrapper.eq("option_code", model.getOptionCode());

        // 重复校验排除自身
        if(StringUtils.isNotEmpty(model.getId())){
            wrapper.notIn(MyBatisConstants.FIELD_ID, model.getId());
        }

        return super.count(wrapper);
    }

    /**
     * 清除缓存
     * @param optionList
     */
    private void clearCache(List<OptionsModel> optionList){
        // 清空缓存
        if(CollUtil.isNotEmpty(optionList)){
            int cacheCount = 0;
            for (OptionsModel model : optionList) {
                cacheCount++;
                boolean tmp = OptionsUtil.refreshOption(model);
                if(tmp){
                    cacheCount--;
                }
            }
            // 判断删除状态
            if(cacheCount != 0){
                // 删除缓存失败
                throw new ServiceException(CoreMsg.CACHE_DEL_EXCEPTION);
            }
        }
    }

}
