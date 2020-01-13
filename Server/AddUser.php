<?php
  include "/var/www/inc/dbinfo.inc";
  $conn = mysqli_connect(DB_SERVER, DB_USERNAME, DB_PASSWORD, DB_DATABASE);

  $deviceID = $_POST["DeviceID"];
  if (empty($deviceID)) {
    die("Empty DeviceID");
    // prevents adding blank users using this PHP link.
  }

  $query = "insert into Users (DeviceID, LastEndDateTime) values (?, '2020-01-01 00:00:00:000');";
  $stmt = $conn->prepare($query);
  $stmt->bind_param("s", $deviceID);
  $stmt->execute();

  echo "New user is added!";
?>

