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
package org.opsli.modulars.gentest.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.opsli.modulars.gentest.user.entity.TestUser;

/**
* @BelongsProject: opsli-boot
* @BelongsPackage: org.opsli.modulars.gentest.user.mapper
* @Author: 周鹏程
* @CreateTime: 2020-11-22 12:12:05
* @Description: 某系统用户 Mapper
*/
@Mapper
public interface TestUserMapper extends BaseMapper<TestUser> {

}