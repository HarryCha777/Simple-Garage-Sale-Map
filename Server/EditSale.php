<?php
  include "/var/www/inc/dbinfo.inc";
  $conn = mysqli_connect(DB_SERVER, DB_USERNAME, DB_PASSWORD, DB_DATABASE);

  $deviceID = $_POST["DeviceID"];
  $title = $_POST["Title"];
  $description = $_POST["Description"];
  $image1 = $_POST["Image1"];
  $image2 = $_POST["Image2"];
  $image3 = $_POST["Image3"];
  $email = $_POST["Email"];
  $phoneNumber = $_POST["PhoneNumber"];
  $postedDateTime = $_POST["PostedDateTime"];
  $startDateTime = $_POST["StartDateTime"];
  $endDateTime = $_POST["EndDateTime"];
  if (empty($title)) {
    die("Empty Title");
    // prevents editing blank sales using this PHP link.
  }

  $query = "update Sales set Title = ?, Description = ?, Image1 = ?, Image2 = ?, Image3 = ?, Email = ?, PhoneNumber = ?, StartDateTime = ?, EndDateTime = ? where PostedDateTime = ?;";
  $stmt = $conn->prepare($query);
  $stmt->bind_param("ssssssssss", $title, $description, $image1, $image2, $image3, $email, $phoneNumber, $startDateTime, $endDateTime, $postedDateTime);
  $stmt->execute();

  $query = "update Users set LastEndDateTime = ? where DeviceID = ?;";
  $stmt = $conn->prepare($query);
  $stmt->bind_param("ss", $endDateTime, $deviceID);
  $stmt->execute();

  echo "Sale is edited!";
?>
