package com.dgut.controller;

import com.dgut.entity.*;
import com.dgut.service.ForderService;
import com.dgut.service.StudentService;
import com.dgut.service.TeacherRequirementService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/teacherRequirement")
public class TeacherRequirementController {
	
	private final Integer pageSize=5;

	private Logger logger = LoggerFactory.getLogger(TeacherRequirementController.class);
	
	@Autowired
	TeacherRequirementService teacherRequirementService;
	
	@Autowired
	StudentService studentService;
	
	@Autowired
	ForderService forderService;
	
	/**
	 * 学员注册时发布的
	 * @param teacherRequirement
	 * @return
	 */
	@RequestMapping("/register")
	@ResponseBody
	public Msg register(TeacherRequirement teacherRequirement){
		System.out.println(teacherRequirement);
		if(teacherRequirementService.insertSelective(teacherRequirement)==1){
			Forder forder=new Forder();
			forder.setReleaseDate(new Date());
			forder.setStatus(0);
			forder.setStudentId(teacherRequirement.getStudentId());
			forder.setSubject(teacherRequirement.getSubject());
			forderService.insertSelective(forder);
			return Msg.success("success");
		}
		return Msg.error("发布家教信息失败");
	}
	
	/**
	 * 发布家教信息
	 * @param teacherRequirement
	 * @return
	 */
	@RequestMapping(value = "/save",method = RequestMethod.POST)
	@ResponseBody
	public Msg save(HttpSession session ,TeacherRequirement teacherRequirement){
		System.out.println(teacherRequirement);
		Student student=(Student) session.getAttribute("student");
		teacherRequirement.setStudentId(student.getId());
		teacherRequirement.setReleaseTime(new Date());
		if(teacherRequirementService.insertSelective(teacherRequirement)==1){
			return Msg.success("success");
		}
		return Msg.error("发布家教信息失败");
	}
	//学员库中查询的家教信息，查询发布中和已关闭的订单，按时间降序
	@RequestMapping(value = "/selectAllTeacherRequire")
	@ResponseBody
	public Msg selectAllTeacherRequire(@RequestParam(value="pageNum",defaultValue="1")Integer pageNum,String area,String grade,Integer sex,Integer identity,String subject,String studentId){
		PageHelper.startPage(pageNum, pageSize);
		Integer studentId1;
		if("".equals(studentId)){
		    studentId1 = null;
        }else{
		    studentId1 = Integer.parseInt(studentId);
        }

		List<TeacherRequirement> list=teacherRequirementService.selectTeacherRequirementsByExample(area, grade, sex, identity, subject,studentId1);
		if(!list.isEmpty()&&list.size()!=0){
			PageInfo<TeacherRequirement> pageInfo=new PageInfo<>(list);
			return Msg.success("").add("pageInfo", pageInfo);
		}
		return Msg.error("");
	}
	
	@RequestMapping(value = "/selectTeacherRequirement",method = RequestMethod.POST)
	@ResponseBody
	public Msg selectTeacherRequirement(Integer id){
		TeacherRequirement teacherRequirement=teacherRequirementService.selectByPrimaryKey(id);
		if(teacherRequirement!=null){
			Student student=studentService.selectByPrimaryKey(teacherRequirement.getStudentId());
			return Msg.success("").add("req", teacherRequirement).add("student", student);
		}
		return Msg.error("");
	}


	@RequestMapping("/selectTeacherRequirementByStudentId")
    @ResponseBody
	public Msg selectTeacherRequirementByStudentId(Integer studentId){
        List<TeacherRequirement> teacherRequirements = teacherRequirementService.selectTeacherRequirementByStudentId(studentId);
        List<Integer> ids = new ArrayList<>();
        teacherRequirements.forEach(x->{ids.add(x.getId());});
        if(ids.isEmpty()){
            return Msg.error("对不起，您来晚了，该学员的家教订单已被他人预定了");
        }else{
            return Msg.success("").add("list",ids);
        }
    }

    @RequestMapping("/selectRecommendTeacherRequire")
	@ResponseBody
    public Msg selectRecommendTeacherRequire(Integer id){
		List<TeacherRequirement> teacherRequirements = teacherRequirementService.recommendTeacherRequirement(id);
		return Msg.success("").add("list",teacherRequirements);
	}

}
