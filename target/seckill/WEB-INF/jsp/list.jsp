<%--
  Created by IntelliJ IDEA.
  User: LOUK
  Date: 2018/4/15
  Time: 12:26
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="common/tag.jsp"%>
<!DOCTYPE html>
<html>
<head>
    <title>秒杀列表页</title>
    <%@include file="common/head.jsp" %>
</head>

<body>
    <div class="container">
        <div class="panel panel-default">
            <div class="panel-heading text-center">
              <h2>秒杀列表</h2>
            </div>
            <div class="panel-center">
              <table class="table table-hover">
                <thead>
                  <tr>
                      <th>名称</th>
                      <th>库存</th>
                      <th>开始时间</th>
                      <th>结束时间</th>
                      <th>创建时间</th>
                      <th>详情页</th>
                  </tr>
                </thead>
                <tbody>
                <c:forEach var="sk" items="${list}">
                    <tr>
                        <td>${sk.name}</td>
                        <td>${sk.number}</td>
                        <td>
                            <fmt:formatDate value="${sk.startTime}" pattern="yyyy-MM-dd HH:mm:ss"/>
                        </td>
                        <td>
                            <fmt:formatDate value="${sk.endTime}" pattern="yyyy-MM-dd HH:mm:ss"/>
                        </td>
                        <td>
                            <fmt:formatDate value="${sk.createTime}" pattern="yyyy-MM-dd HH:mm:ss"/>
                        </td>
                        <td>
                            <a class="btn btn-info" href="${pageContext.request.contextPath}/seckill/${sk.seckillId}/detail" target="_blank">详情</a>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
              </table>
            </div>
            <nav style="text-align: center">
                <ul class="pagination">
                    <c:forEach begin="1" end="${page.pages}" varStatus="status" var="item">
                        <li><a href="${pageContext.request.contextPath}/seckill/list?startPage=${status.index}">${status.index}</a></li>
                    </c:forEach>
                </ul>
            </nav>
            <h4 style="text-align: right">当前第<span style="color:#ff1493">${page.pageNum}</span>页&nbsp;&nbsp;&nbsp;&nbsp;</h4>
            <h4 style="text-align: right">每页${page.pageSize}条数据，一共${page.pages}页&nbsp;&nbsp;&nbsp;&nbsp;</h4>
        </div>
    </div>
</body>
<!-- jQuery文件。务必在bootstrap.min.js 之前引入 -->
<script src="https://cdn.bootcss.com/jquery/2.1.1/jquery.min.js"></script>
<script>

</script>
<!-- 最新的 Bootstrap 核心 JavaScript 文件 -->
<script src="https://cdn.bootcss.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
</html>

