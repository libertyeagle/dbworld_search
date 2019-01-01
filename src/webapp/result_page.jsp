<%@ page import="com.libertyeagle.DBWorldMessage" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.concurrent.TimeUnit" %>
<%@ page import="com.libertyeagle.QueryProcessor" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.TimeZone" %><%--
  Created by IntelliJ IDEA.
  User: libertyeagle
  Date: 2019/1/1
  Time: 2:51 AM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%!
    Locale LOCALE = Locale.US;
    SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", LOCALE);
    QueryProcessor query_processor = new QueryProcessor();
%>
<%!
    String add_conference_result(DBWorldMessage message) {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("MST"));
        StringBuilder content_builder = new StringBuilder();
        content_builder.append("<div class=\"row align-items-center\">\n");
        content_builder.append("<div class=\"col my-2\">\n");
        content_builder.append("<div class=\"card text-center\">\n" +
                "<div class=\"card-header\">\n");
        content_builder.append(message.message_type);
        content_builder.append(
                "</div>\n" +
                        "<div class=\"card-body\">\n" +
                        " <h5 class=\"card-title\">");
        content_builder.append(message.subject_title);
        content_builder.append("</h5>\n" +
                "<p class=\"card-text\">\n");

        content_builder.append("<b>LOCATION: </b>");
        content_builder.append(message.conf_info.location);
        content_builder.append("<br>\n");
        content_builder.append("<b>DEADLINE: </b>");
        if (message.deadline.getTime() != 0) {
            content_builder.append(DATE_FORMAT.format(message.deadline));
        } else {
            content_builder.append("N/A");
        }
        content_builder.append("<br>\n");

        content_builder.append("<b>CONF. START DATE: </b>");
        if (message.conf_info.start_date.getTime() != 0) {
            content_builder.append(DATE_FORMAT.format(message.conf_info.start_date));
        } else {
            content_builder.append("N/A");
        }
        content_builder.append("<br>\n");
        content_builder.append("<b>CONF. END DATE: </b>");
        if (message.conf_info.end_date.getTime() != 0) {
            content_builder.append(DATE_FORMAT.format(message.conf_info.end_date));
        } else {
            content_builder.append("N/A");
        }
        content_builder.append("<br>\n");
        content_builder.append("<b><a href=\"");
        content_builder.append(message.detail_url);
        content_builder.append("\">CLICK HERE FOR DETAIL</a></b><br>");
        content_builder.append("<b>TOPICS:</b><br>\n");
        content_builder.append(message.conf_info.topics.replace("\n", "<br>"));
        content_builder.append("</p></div>\n" +
                "<div class=\"card-footer text-muted\">");
        if (message.date_sent.getTime() != 0) {
            long diff = new Date().getTime() - message.date_sent.getTime();
            long days_diff = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
            String footer = "Published " + days_diff + " days ago";
            content_builder.append(footer);
        } else
            content_builder.append("Missing Message Sent Date");
        content_builder.append("</div>\n" +
                "</div>\n" +
                "</div>\n" +
                "</div>");
        return content_builder.toString();
    }

    String add_other_result(DBWorldMessage message) {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        StringBuilder content_builder = new StringBuilder();
        content_builder.append("<div class=\"row align-items-center\">\n");
        content_builder.append("<div class=\"col my-2\">\n");
        content_builder.append("<div class=\"card text-center\">\n" +
                "<div class=\"card-header\">\n");
        content_builder.append(message.message_type);
        content_builder.append(
                "</div>\n" +
                        "<div class=\"card-body\">\n" +
                        " <h5 class=\"card-title\">");
        content_builder.append(message.subject_title);
        content_builder.append("</h5>\n" +
                "<p class=\"card-text\">\n");

        content_builder.append("<b>SENDER: </b>");
        content_builder.append(message.sender);
        content_builder.append("<br>\n");
        content_builder.append("<b>DEADLINE: </b>");
        if (message.deadline.getTime() != 0) {
            content_builder.append(DATE_FORMAT.format(message.deadline));
        } else {
            content_builder.append("N/A");
        }
        content_builder.append("<br>\n");
        content_builder.append("<b><a href=\"");
        content_builder.append(message.detail_url);
        content_builder.append("\">CLICK HERE FOR DETAIL</a></b><br>");
        content_builder.append("<b>MESSAGE:</b><br>\n");
        if (message.description.length() > 500) {
            content_builder.append(message.description.substring(0, 500).replace("\n", "<br>"));
            content_builder.append(" ...");
        } else {
            content_builder.append(message.description.replace("\n", "<br>"));
        }

        content_builder.append("</h5></div>\n" +
                "<div class=\"card-footer text-muted\">");
        if (message.date_sent.getTime() != 0) {
            long diff = new Date().getTime() - message.date_sent.getTime();
            long days_diff = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
            String footer = "Published " + days_diff + " days ago";
            content_builder.append(footer);
        } else
            content_builder.append("Missing Message Sent Date");
        content_builder.append("</div>\n" +
                "</div>\n" +
                "</div>\n" +
                "</div>");
        return content_builder.toString();
    }

    public void jspInit() {
        // Initialization code...
        query_processor.init();
    }
%>
<%
    String query_text = request.getParameter("query");
    int search_type = Integer.parseInt(request.getParameter("search_type"));
    String page_request = request.getParameter("page");
    int page_index;
    if (page_request == null)
        page_index = 0;
    else
        page_index = Integer.parseInt(page_request);
    if (page_index < 0) page_index = 0;
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <link rel="stylesheet" href="bootstrap/css/bootstrap.css">
    <script type="text/javascript" src="bootstrap/js/bootstrap.js"></script>
    <script src="https://code.jquery.com/jquery-3.3.1.slim.min.js"
            integrity="sha384-q8i/X+965DzO0rT7abK41JStQIAqVgRVzpbzo5smXKp4YfRvH+8abtTE1Pi6jizo"
            crossorigin="anonymous"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.6/umd/popper.min.js"
            integrity="sha384-wHAiFfRlMFy6i5SRaxvfOCifBUQy1xHdJ/yoi7FRNXMRBu5WHdZYu1hA6ZOblgut"
            crossorigin="anonymous"></script>

    <link rel="stylesheet" href="result_style.css">
    <title>DBWorld Search</title>
    <header class="masthead">
        <nav class="navbar navbar-default" role="navigation">
            <div class="container-fluid">
                <div class="navbar-header">
                    <a class="navbar-brand" href="https://research.cs.wisc.edu/dbworld/browse.html">DBWorld</a>
                </div>
            </div>
        </nav>
    </header>
</head>
<body>
<div class="container">
    <div class="row">
        <div class="col mt-3">
            <center><h1>DBWorld Search</h1><h5>by libertyeagle</h5></center>
        </div>
    </div>
    <div class="row">
        <form method="POST" action="result_page.jsp">
            <div class="col col-lg-3 my-3 mx-auto">
                <select class="custom-select" name="search_type" id="inputGroupSelect01">
                    <option value="1" selected>Conference (Title, Text)</option>
                    <option value="2">Conference (Date)</option>
                    <option value="3">Conference (Location)</option>
                    <option value="4">Conference (Topics)</option>
                    <option value="5">Conference (Deadline)</option>
                    <option value="6">Job</option>
                    <option value="7">Journal</option>
                    <option value="8">Others</option>
                </select>
            </div>
            <div class="col col-lg-7 my-3 mx-auto">
                <input type="text" name="query" id="inputEmail" class="form-control" value="<%=query_text%>"
                       required
                       autofocus>
            </div>
            <div class="col col-lg-2 my-3 mx-auto">
                <button type="submit" class="btn btn-primary btn-block">Search</button>
            </div>
        </form>
    </div>
</div>
<%
    ArrayList<DBWorldMessage> results = new ArrayList<DBWorldMessage>();
    switch (search_type) {
        case 1:
            results = query_processor.search_conference_default(query_text);
            break;
        case 2:
            results = query_processor.search_conf_by_conf_date(query_text);
            break;
        case 3:
            results = query_processor.search_conf_by_location(query_text);
            break;
        case 4:
            results = query_processor.search_conf_by_topics(query_text);
            break;
        case 5:
            results = query_processor.search_conf_by_deadline(query_text);
            break;
        case 6:
            results = query_processor.search_job_default(query_text);
            break;
        case 7:
            results = query_processor.search_journal_default(query_text);
            break;
        default:
            results = query_processor.search_others_default(query_text);
    }
%>
<div class="container">
    <div class="row align-items-center">
        <div class="col my-3">
            Found <span style="color:#00b894;font-weight: bold"><%=results.size()%></span> items!
        </div>
    </div>
    <%
        int result_size = results.size();
        int total_pages = (int) Math.ceil((double) (result_size) / 10);
        if (page_index >= total_pages) page_index = 0;
        if (search_type <= 5) {
            for (int index = page_index * 10; (index < (page_index + 1) * 10) && (index < result_size); index++) {
                DBWorldMessage result = results.get(index);
                String content = add_conference_result(result);
                out.println(content);
            }
        } else {
            for (int index = page_index * 10; (index < (page_index + 1) * 10) && (index < result_size); index++) {
                DBWorldMessage result = results.get(index);
                String content = add_other_result(result);
                out.println(content);
            }
        }
        String prev_navlink = "nav-link";
        if (page_index == 0) prev_navlink = "nav-link disabled";
        String next_navlink = "nav-link";
        if (page_index == total_pages - 1) next_navlink = "nav-link disabled";
    %>
</div>
<div class="container">
    <div class="row align-items-center">
        <div class="col mt-3">
            <center>Page <span style="color:#576574;font-weight:bold;"> <%=page_index + 1%> / <%=total_pages > 0 ? total_pages : 1%></span>
            </center>
        </div>
    </div>
    <div class="row align-items-center">
        <div class="col">
            <ul class="nav justify-content-center my-1">
                <li class="nav-item">
                    <a class="<%=prev_navlink%>"
                       href="result_page.jsp?page=<%=page_index - 1%>&search_type=<%=search_type%>&query=<%=query_text%>">Previous</a>
                </li>
                <li class="nav-item">
                    <a class="<%=next_navlink%>"
                       href="result_page.jsp?page=<%=page_index + 1%>&search_type=<%=search_type%>&query=<%=query_text%>">Next</a>
                </li>
            </ul>
        </div>
    </div>
</body>
</html>
