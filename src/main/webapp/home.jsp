<%--
  Created by IntelliJ IDEA.
  User: libertyeagle
  Date: 2019/1/1
  Time: 3:06 AM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
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

    <link rel="stylesheet" href="home_style.css">
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
<div class="main">
    <div class="container">
        <div class="row">
            <div class="col">
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
                    <input type="text" name="query" id="inputEmail" class="form-control" placeholder="What to search?"
                           required
                           autofocus>
                </div>
                <div class="col col-lg-2 my-3 mx-auto">
                    <button type="submit" class="btn btn-primary btn-block">Search</button>
                </div>
            </form>
        </div>
        <div class="row">
            <div class="col-lg">
                <div class="card border-info my-3 mr-3">
                    <div class="card-header">Features</div>
                    <div class="card-body text-info">
                        <h5 class="card-title">Lightweight</h5>
                        <p class="card-text">Extreme lightweight DBWorld search engine based on Apache Lucene &
                            Tomcat.</p>
                    </div>
                </div>
            </div>
            <div class="col-lg">
                <div class="card border-info my-3 ml-1 mr-1">
                    <div class="card-header">Features</div>
                    <div class="card-body text-info">
                        <h5 class="card-title">Multifunctional</h5>
                        <p class="card-text">Supports different kinds of search, including search conference by topics,
                            dates, and serach jobs, journals ...</p>
                    </div>
                </div>
            </div>
            <div class="col-lg">
                <div class="card border-info my-3 ml-3">
                    <div class="card-header">Features</div>
                    <div class="card-body text-info">
                        <h5 class="card-title">Fast & Accurate</h5>
                        <p class="card-text">Achieves fast and accurate search through using Apache Lucene and Stanford
                            CoreNLP.</p>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>