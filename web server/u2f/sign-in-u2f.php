<?php
/**
 * Copyright (c) 2014 Yubico AB
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   * Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided
 *     with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * This is a minimal example of U2F registration and authentication.
 * The data that has to be stored between registration and authentication
 * is stored in browser localStorage, so there's nothing real-world
 * about this.
 */
require_once('./php-u2flib-server-master/src/u2flib_server/U2F.php');
$scheme = isset($_SERVER['HTTPS']) ? "https://" : "http://";
$u2f = new u2flib_server\U2F($scheme . $_SERVER['HTTP_HOST']);
function fixupArray($data) {
	$ret = array();
	$decoded = json_decode($data);
	foreach ($decoded as $d) {
		$ret[] = json_encode($d);
	}
	return $ret;
}

?>



<html>
<head>
<title>u2f Sign In Form</title>
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link href="css/style.css" rel="stylesheet" type="text/css" media="all" /> 
<script src="js/jquery-2.2.3.min.js"></script>


    <script src="../assets/u2f-api.js"></script>

    <script>
        function addRegistration(reg) {
            var existing = localStorage.getItem('u2fregistration');
            var regobj = JSON.parse(reg);
            var data = null;
            if(existing) {
                data = JSON.parse(existing);
                if(Array.isArray(data)) {
                    for (var i = 0; i < data.length; i++) {
                        if(data[i].keyHandle === regobj.keyHandle) {
                            data.splice(i,1);
                            break;
                        }
                    }
                    data.push(regobj);
                } else {
                    data = null;
                }
            }
            if(data == null) {
                data = [regobj];
            }
            localStorage.setItem('u2fregistration', JSON.stringify(data));
        }
	</script>
		
		
    <?php
    if($_SERVER['REQUEST_METHOD'] === 'POST') {
        if(isset($_POST['startAuthenticate'])) {
            $regs = json_decode($_POST['registrations']);
            $data = $u2f->getAuthenticateData($regs);
            echo "<script>";
            echo "var registrations = " . $_POST['registrations'] . ";\n";
            echo "var request = " . json_encode($data) . ";\n";
            echo '
            window.onload = function() {
                console.log("sign: ", request);
                document.getElementById("ins").innerHTML = "Insert and tap on the blinking u2f authenticator";
                u2f.sign(request, function(data) {
                    document.getElementById("username").value = "'.$_POST['username'].'";
                    var form = document.getElementById("form");
                    var reg = document.getElementById("doAuthenticate");
                    var req = document.getElementById("request");
                    var regs = document.getElementById("registrations");
                    console.log("Authenticate callback", data);
                    reg.value=JSON.stringify(data);
                    req.value=JSON.stringify(request);
                    regs.value=JSON.stringify(registrations);
                    form.submit();
                });
            };
            </script>';
        } else if($_POST['doAuthenticate']) {
            $reqs = json_decode($_POST['request']);
            $regs = json_decode($_POST['registrations']);
            try {
                $data = $u2f->doAuthenticate($reqs, $regs, json_decode($_POST['doAuthenticate']));
                echo "<script>";
                echo "var registration = '" . json_encode($data) . "';\n";
                echo "addRegistration(registration);\n";
                session_start();
                $_SESSION['username']=$_POST['username'];
                echo "window.location.href='hello-u2f.php'";
                echo "</script>";
            } catch(u2flib_server\Error $e) {
                echo "<script>alert('error:" . $e->getMessage() . "');\n</script>";
            }
        }
    }
    ?>
		
</head>
<body>
	<div class="main-agileits">
        <h1>u2f Sign In Form</h1>
        <div class="instructions">go to <a href="../2fysh/sign-in.php">2fysh page</a></div>
		<div class="mainw3-agileinfo form">
			<div id="login">
				<div class="instructions" id="ins"></div>
                <form method="POST" id="form">
                    <div class="field-wrap">
                        <input type="text" placeholder="Username" name="username" id="username" required autocomplete="off"/>
                    </div> 
                    <input type="hidden" name="doAuthenticate" id="doAuthenticate"/>
                    <input type="hidden" name="request" id="request"/>
                    <input type="hidden" name="registrations" id="registrations"/>
                    <p class="instructions2">
                        <span id="registered">0</span> Authenticators currently registered.
                    </p>
                    <p class="forgot">Don't have account? <a href="/u2f/sign-up-u2f.php">Sign Up</a></p> 
                    <button class="button button-block" name="startAuthenticate" type="submit" id="startAuthenticate">Sign In</button>
                </form>
            </div>
        </div>	
    </div>
</body>




<script>
    var reg = localStorage.getItem('u2fregistration');
    var auth = document.getElementById('startAuthenticate');
    if(reg == null) {
        auth.disabled = true;
    } else {
        var regs = document.getElementById('registrations');
        decoded = JSON.parse(reg);
        if(!Array.isArray(decoded)) {
            auth.disabled = true;
        } else {
            regs.value = reg;
            console.log("set the registrations to : ", reg);
            var regged = document.getElementById('registered');
            regged.innerHTML = decoded.length;
        }
    }
</script>
</body>
</html>
