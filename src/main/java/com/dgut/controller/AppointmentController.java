package com.dgut.controller;

import com.dgut.entity.Appointment;
import com.dgut.entity.Msg;
import com.dgut.entity.Student;
import com.dgut.entity.Teacher;
import com.dgut.service.AppointmentService;
import com.dgut.service.TeacherRequirementService;
import com.dgut.service.TeacherService;
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
@RequestMapping("/appointment")
public class AppointmentController {

    private final Integer pageSize = 5;

    private Logger logger = LoggerFactory.getLogger(AppointmentController.class);

    @Autowired
    AppointmentService appointmentService;

    @Autowired
    TeacherService teacherService;

    @Autowired
    TeacherRequirementService teacherRequirementService;

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ResponseBody
    public Msg save(Appointment appointment) {
        if (appointmentService.insertSelective(appointment) == 1) {
            return Msg.success("");
        } else {
            return Msg.error("");
        }
    }


    @RequestMapping(value = "/selectAllAppointment", method = RequestMethod.POST)
    @ResponseBody
    public Msg selectAllAppointment(HttpSession session, int organiser, @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        PageHelper.startPage(pageNum, pageSize);
        //获取当前登录人的身份
        Integer identity = (Integer) session.getAttribute("identity");
        List<Appointment> list = new ArrayList<>();
        PageInfo<Appointment> pageInfo = null;
        //如果登录人为学员
        if (identity == 1) {
            Student student = (Student) session.getAttribute("student");
            list = appointmentService.selectAllStudentAppointment(organiser, student.getId());

        } else if (identity == 2) {
            Teacher teacher = (Teacher) session.getAttribute("teacher");
            list = appointmentService.selectAllTeacherAppointment(organiser, teacher.getId());
        }

        if (list != null && list.size() != 0) {
            pageInfo = new PageInfo<>(list);
            return Msg.success("").add("pageInfo", pageInfo);
        }

        return Msg.error("");

    }

    @RequestMapping(value = "/confirm", method = RequestMethod.POST)
    @ResponseBody
    public Msg confirmAppointment(int id) {
        logger.info("当前预约id为：{}", id);
        Appointment appointment = new Appointment();
        appointment.setId(id);
        appointment.setStatus(1);
        if (appointmentService.updateByPrimaryKeySelective(appointment) == 1) {
            return Msg.success("");
        }
        return Msg.error("预约确认失败");
    }


    //在线预约 供教员预约
    @RequestMapping("/applyAppointmentForTeacher")
    @ResponseBody
    public Msg applyAppointment(HttpSession session, Integer teacherRequirementId, Integer anotherId) {
        //先判断该家教单是否已被关闭
        if (teacherRequirementId != null) {
            if (teacherRequirementService.checkClosed(teacherRequirementId)) {
                return Msg.error("该家教单已被关闭，无法预约！！！");
            }
        }
        //获取当前登录人身份
        Integer organiser = (Integer) session.getAttribute("identity");
        if (organiser == null) {
            return Msg.error("请先登录！！！");
        }else if(organiser == 1){
            return Msg.error("家教单只能由教员预约，学员无法预约");
        }
        Appointment appointment = new Appointment();
        Teacher teacher = (Teacher) session.getAttribute("teacher");
        appointment.setTeacherId(teacher.getId());
        appointment.setTeacherName(teacher.getName());
        appointment.setStudentId(anotherId);
        appointment.setTeacherRequirementId(teacherRequirementId);
        appointment.setOrganiser(organiser);
        appointment.setAppointmentDate(new Date());
        appointmentService.insertSelective(appointment);
        return Msg.success("预约成功！！！");
    }
}
