package com.simon.controller;

import com.simon.common.controller.BaseController;
import com.simon.common.domain.EasyUIDataGridResult;
import com.simon.common.domain.ResultMsg;
import com.simon.common.domain.UserEntity;
import com.simon.common.plugins.quartz.QuartzManage;
import com.simon.model.QuartzJob;
import com.simon.service.QuartzJobService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
* quartz任务
*
* @author SimonSun
* @date 2018-12-22
**/
@Slf4j
@Api(description = "quartz任务")
@Controller
@RequestMapping("/api/quartzJobs")
public class QuartzJobController extends BaseController{

    @Autowired
    private QuartzJobService quartzJobService;

    @Autowired
    private QuartzManage quartzManage;

    @ApiIgnore
    @ApiOperation(value = "列表页面")
    @GetMapping(params = "easyui-list")
    public String getEasyUIList(){
        return "easyui/quartz_job";
    }

    @ApiIgnore
    @ApiOperation(value = "列表数据")
    @GetMapping("easyui/list")
    @ResponseBody
    public EasyUIDataGridResult<QuartzJob> getEasyUIList(
            @ApiParam(value = "类名") @RequestParam(required = false) String beanName,
            @ApiParam(value = "job状态") @RequestParam(required = false) Integer jobStatus,
            @ApiParam(value = "页码", defaultValue = "1", required = true) @RequestParam Integer pageNo,
            @ApiParam(value = "每页条数", defaultValue = "10", required = true)@RequestParam Integer pageSize,
            @ApiParam(value = "排序")@RequestParam(required = false, defaultValue = "") String orderBy){
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("beanName", beanName);
        params.put("jobStatus", jobStatus);
        return new EasyUIDataGridResult<>(quartzJobService.getList(params, pageNo, pageSize, orderBy));
    }

    @ApiOperation(value = "新增")
    @PostMapping
    @ResponseBody
    public ResultMsg add(@RequestBody QuartzJob body){
        quartzJobService.insertSelective(body);
        return ResultMsg.success();
    }

    @ApiOperation(value = "修改")
    @PatchMapping
    @ResponseBody
    public ResultMsg update(@RequestBody QuartzJob body){
        quartzJobService.updateByPrimaryKeySelective(body);
        return ResultMsg.success();
    }

    @ApiOperation(value = "删除")
    @DeleteMapping("/id/{ids}")
    @ResponseBody
    public ResultMsg delete(@PathVariable String ids){
        quartzJobService.deleteByIds(ids);
        return ResultMsg.success();
    }

    //@PreAuthorize("isAuthenticated()")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SU')")
    @ApiOperation(value = "定时任务操作（启动，暂停）")
    @PostMapping("/id/{id}/jobStatus/{jobStatus}")
    @ResponseBody
    public ResultMsg operation(
            Authentication authentication,
            @PathVariable Long id,
            @ApiParam(value = "job状态[0:off, 1:on]", required = true)@PathVariable int jobStatus) throws ClassNotFoundException, InstantiationException, SchedulerException, IllegalAccessException {
        Object principal = authentication.getPrincipal();
        UserEntity userEntity = null;
        if(principal instanceof UserEntity){
            userEntity = (UserEntity)principal;
        }

        QuartzJob quartzJob = quartzJobService.findById(id);

        if(0 == jobStatus){
            quartzManage.pauseJob(quartzJob);
        }else{
            quartzManage.resumeJob(quartzJob);
        }

        quartzJob.setJobStatus(jobStatus);
        quartzJob.setUpdateBy(userEntity.getId());
        quartzJob.setUpdateDate(new Date());
        quartzJobService.updateByPrimaryKeySelective(quartzJob);
        return ResultMsg.success();
    }
}