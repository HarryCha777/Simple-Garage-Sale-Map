<?php
  include "/var/www/inc/dbinfo.inc";
  $conn = mysqli_connect(DB_SERVER, DB_USERNAME, DB_PASSWORD, DB_DATABASE);

  $deviceID = $_POST["DeviceID"];
  $postedDateTime = $_POST["PostedDateTime"];
  if (empty($deviceID)) {
    die("Empty DeviceId");
    // prevents deleting blank sales using this PHP link.
  }

  $query = "delete from Sales where PostedDateTime = ? limit 1;";
  $stmt = $conn->prepare($query);
  $stmt->bind_param("s", $postedDateTime);
  $stmt->execute();

  $query = "update Users set LastEndDateTime = '2020-01-01 00:00:00:000' where DeviceID = ?;";
  $stmt = $conn->prepare($query);
  $stmt->bind_param("s", $deviceID);
  $stmt->execute();

  echo "Sale is deleted!";
?>

