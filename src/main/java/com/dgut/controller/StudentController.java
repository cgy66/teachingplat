package com.dgut.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.dgut.entity.*;
import com.dgut.utils.IPAddressUtil;
import com.dgut.utils.RandomValidateCode;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


import com.dgut.service.StudentService;
import com.dgut.service.TeacherService;

import java.util.Date;


@Api
@Controller
public class StudentController {

	private Logger logger = LoggerFactory.getLogger(StudentController.class);

	@Autowired
	StudentService studentService;
	
	@Autowired
	TeacherService teacherService;

	@Autowired
	RandomValidateCode randomValidateCode;
	
	


	@RequestMapping(value = "/logout",method = RequestMethod.POST)
	@ResponseBody
	public void logout(HttpSession session){
		Integer identity=(Integer) session.getAttribute("identity");
		System.out.println("------identity-------:"+identity);
		if((Integer)session.getAttribute("identity")==1){
			Student student=(Student) session.getAttribute("student");
			studentService.updateByPrimaryKeySelective(student);
			session.removeAttribute("student");
		}else{
			Teacher teacher = (Teacher) session.getAttribute("teacher");
			teacherService.updateByPrimaryKeySelective(teacher);
			session.removeAttribute("teacher");
		}
	}
	
	
	
	@RequestMapping("/student/login")
	@ResponseBody
	public Msg login(HttpServletRequest request, String email, String password) {
		Student student;
		HttpSession session = request.getSession();
		if ((student = studentService.login(email, password)) != null) {
			if (student.getIsabled() == 0) {
				return Msg.error("您的邮箱还没有经过验证");
			} else if (student.getIsabled() == 1) {
				return Msg.error("您的账户已被禁用！！！");
			} else {
				student.setLoginTimes((student.getLoginTimes()==null?0:student.getLoginTimes())+1);
				student.setLastTime(new Date());
				student.setLastIp(IPAddressUtil.getIpAdrress(request));
				session.removeAttribute("teacher");
				session.setAttribute("student", student);
				session.setAttribute("identity", 1);
				System.out.println("student:"+student);
				return Msg.success("登陆成功");
			}
		} else {
			return Msg.error("登录失败,请检查您的密码和邮箱是否正确");
		}

	}

	@RequestMapping(value = "/student/register",method = RequestMethod.POST)
	@ResponseBody
	public Msg register(HttpServletRequest request,Student student,String pcode) {
		// 获取验证码
		String code = (String) request.getSession().getAttribute("RANDOM_CODE_KEY");
		if(!code.equals(pcode)){
			return Msg.error("验证码错误");
		}
		Integer id=studentService.register(student);
		if (id!=null) {
			request.getSession().removeAttribute("RANDOM_CODE_KEY");
			return Msg.success("注册成功").add("id", id);
		}
		return Msg.error("注册失败");
	}

	@RequestMapping("/pcode")
	public void pcode(HttpServletRequest request, HttpServletResponse response) {
		randomValidateCode.getRandcode(request, response);
	}

	@ResponseBody
	@RequestMapping("/docode")
	public Msg doLogin(HttpServletRequest request, HttpServletResponse response, @RequestParam String pcode) {
		// 获取验证码
		String code = (String) request.getSession().getAttribute("RANDOM_CODE_KEY");
		request.getSession().removeAttribute("RANDOM_CODE_KEY");
		if (code.equals(pcode)) {
			return null;
		} else {
			return Msg.error("验证码错误");
		}

	}
	
	@RequestMapping("/GetStudentInformation")
	@ResponseBody
	public Msg GetStudentInfomation(HttpSession session){
		Student student=(Student) session.getAttribute("student");
		if(student!=null){
			Student student2=studentService.selectByPrimaryKey(student.getId());
			session.setAttribute("student", student2);
			return Msg.success("").add("student", student2);
		}
		return Msg.error("");
		
	}
	
	@RequestMapping("/student/getInfo")
	@ResponseBody
	public Msg getStudentInfo(HttpSession session){
		Student student=null;
		if((student=(Student) session.getAttribute("student"))!=null){
			return Msg.success("").add("student", student);
		}
		return Msg.error("");
	}
	
	@RequestMapping("/student/UpdateInformation")
	@ResponseBody
	public Msg UpdateInfomation(HttpSession session,Student student){
		Student student2=(Student) session.getAttribute("student");
		student.setId(student2.getId());
		System.out.println("student:"+student);
		if(studentService.updateByPrimaryKeySelective(student)==1){
			return Msg.success("");
		}
		
		return Msg.error("");
		
	}
	@RequestMapping("/student/changePass")
	@ResponseBody
	public Msg changePass(HttpSession session,String password){
		System.out.println("password:"+password);
		Student student=(Student) session.getAttribute("student");
		student.setPassword(password);
		if(studentService.updateByPrimaryKeySelective(student)==1){
			session.setAttribute("student", student);
			return Msg.success("修改密码成功");
		}
		return Msg.error("修改密码失败");
	}
	
	@RequestMapping("/student/index")
	public String Index(){
		return "redirect:/my?url=student_home";
	}

	/*@RequestMapping("/student/index")
	public String Index(){
		return "student_home";
	}*/

}
