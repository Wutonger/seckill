package org.seckill.web;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.dto.SeckillResult;
import org.seckill.entity.Seckill;
import org.seckill.enums.SeckillStatEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/seckill")
public class SeckillController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private SeckillService seckillService;

    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public String list(@RequestParam(required = false, defaultValue = "1") Integer startPage,
                       @RequestParam(required = false, defaultValue = "3") Integer pageSize,
                       Model model){
        //设置分页参数
        PageHelper.startPage(startPage,pageSize);
        //获取列表页
        List<Seckill> list = seckillService.getSeckillList();
        PageInfo<Seckill> pageInfo = new PageInfo<Seckill>(list);
        model.addAttribute("page",pageInfo);
        model.addAttribute("list",list);
          return "list";
    }

    @RequestMapping(value = "/{seckillId}/detail",method = RequestMethod.GET)
    public String detail(@PathVariable("seckillId") Long seckillId,Model model){
        if(seckillId==null){
            return "redirect:/seckill/list";
        }
        Seckill seckill=seckillService.getById(seckillId);
        if(seckill==null){
            return "forward:/seckill/list";
        }
        model.addAttribute("seckill",seckill);
        return "detail";
    }

    //ajax方法，返回类型为json数据

    @RequestMapping(value = "/{seckillId}/exposer",method = RequestMethod.POST,
                     produces = "application/json;charset=UTF-8")
    @ResponseBody
    public SeckillResult<Exposer> exposer(@PathVariable Long seckillId){
        SeckillResult<Exposer> result;
        try {
            Exposer exposer = seckillService.exportSeckillUrl(seckillId);
            result = new SeckillResult<Exposer>(true,exposer);
        }catch (Exception e){
              logger.info(e.getMessage(),e);
              result = new SeckillResult<Exposer>(false,e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "{seckillId}/{md5}/execution",method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    @ResponseBody
    public SeckillResult<SeckillExecution> execute(
            @PathVariable("seckillId") Long seckillId,
            @CookieValue(value = "killPhone",required = false) Long phone,
            @PathVariable("md5") String md5){
        if(phone==null){
            return new SeckillResult<SeckillExecution>(false,"未注册");
        }
       try{
           SeckillExecution seckillExecution =seckillService.executeSeckill(seckillId,phone,md5);
           return new SeckillResult<SeckillExecution>(true,seckillExecution);
       }catch (RepeatKillException e){
             SeckillExecution execution =new SeckillExecution(seckillId, SeckillStatEnum.REPEAT_KILL);
             return new SeckillResult<SeckillExecution>(true,execution);
       } catch (SeckillCloseException e){
           SeckillExecution execution =new SeckillExecution(seckillId, SeckillStatEnum.END);
           return new SeckillResult<SeckillExecution>(true,execution);
       } catch (Exception e){
           SeckillExecution execution =new SeckillExecution(seckillId, SeckillStatEnum.INNER_ERROR);
           return new SeckillResult<SeckillExecution>(true,execution);
       }
    }

    @RequestMapping(value = "/time/now",method = RequestMethod.GET)
    @ResponseBody
    public SeckillResult<Long> time(){
            Date date =new Date();
            return new SeckillResult(true,date.getTime());
    }
}
