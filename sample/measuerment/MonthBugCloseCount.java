package com.bronzesoft.demo.measurement.index;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.bronzesoft.power.rdbug.model.Bug;
import com.bronzesoft.rdm.platform.PowerAPI;
import com.bronzesoft.rdm.platform.service.measuerment.impl.Calculator;
import com.bronzesoft.rdm.platform.service.measuerment.vo.CalculatorResult;
import com.bronzesoft.rdm.platform.util.DateUtil;
import com.bronzesoft.rdm.platform.util.LogUtil;
import com.bronzesoft.rdm.platform.util.StringUtil;

public class MonthBugCloseCount extends Calculator {

    @SuppressWarnings("unchecked")
    public CalculatorResult month(String objectId, String objectIdType) {
        CalculatorResult result = new CalculatorResult();
        try {
            String sql = "";
            if ("PRD".equals(objectIdType)) { // 缺陷归属为产品
                sql = " b.productId = '" + objectId + "' and";
            }
            else if ("PJT".equals(objectIdType)) { // 缺陷归属为项目
                sql = " b.projectId = '" + objectId + "' and";
            }
            sql = " from Bug b where " + sql + " b.closedTime "
                    + StringUtil.createDBNotEmptyCondition()
                    + " order by b.createdTime asc";

            List<Object> list = PowerAPI.rdmDao().getAllByHQL(sql);

            Bug bug;
            String key;
            int count = 0;
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            
            // 根据缺陷关闭日期，确定每月关闭的缺陷数目
            for (Object o : list) {
                bug = (Bug) o;
                key = DateUtil.getDateTime("yyyy-MM", bug.getClosedTime());

                if (!map.containsKey(key)) {
                    map.put(key, 0);
                }
                count = (Integer) map.get(key);

                map.put(key, count + 1);
            }

            result.setData(map); // key: 月份， value: 缺陷数目
            result.setResult("Y"); // 执行结果，“Y”： 成功， “N”： 失败
            result.setRemark("项目或产品开发周期内，每月解决（BUG）的数目。 "); // 计算描述
        }
        catch (Exception e) {
            LogUtil.error(getClass().getName() + ".month()\n", e);
        }
        return result;
    }
}
