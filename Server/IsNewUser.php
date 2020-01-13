<?php
  include "/var/www/inc/dbinfo.inc";
  $conn = mysqli_connect(DB_SERVER, DB_USERNAME, DB_PASSWORD, DB_DATABASE);

  $deviceID = $_POST["DeviceID"];

  $query = "select DeviceID from Users where DeviceID = ? limit 1;";
  $stmt = $conn->prepare($query);
  $stmt->bind_param("s", $deviceID);
  $stmt->execute();
  $result = $stmt->get_result() or die("Error: " .mysql_error());;

  if (mysqli_num_rows($result) == 1) {
    echo "Existing User";
  } else {
    echo "New User";
  }
?>
