<html>
<head>
<title>Super secret page</title>
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link href="css/style.css" rel="stylesheet" type="text/css" media="all" /> 
</head>
<body>
	<div class="main-agileits">
        <h1>super secret page</h1>
		<div class="mainw3-agileinfo form">
<?php
    session_start();
    if(isset($_SESSION['username'])){
        //echo "session id: ".session_id()."<br/>";
        echo '
            <div class="instructions">
				Hello <span id="name">'. $_SESSION['username'].'</span>!
			</div>
			<div class="instructions">
				Your bank account balance: <span id="name">Rp 12.354,00</span>
			</div>
        ';
        echo '
        <form method="POST">
           <input class="button button-block" type="submit" name="btnLogOut" value="Log Out" />
        </form>';
    }
    else{
        header('Location:sign-in-u2f.php');

        //echo '<script>alert("Please sign in");
        //window.location.href="sign-in-u2f.php";</script>';
    }

    if(isset($_POST['btnLogOut'])){
        session_regenerate_id();
        session_destroy();
        session_unset();

        
        echo '<script>alert("Logging out");
        window.location.href="/u2f/sign-in-u2f.php";</script>';
    }
?>