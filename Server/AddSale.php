<?php
  include "/var/www/inc/dbinfo.inc";
  $conn = mysqli_connect(DB_SERVER, DB_USERNAME, DB_PASSWORD, DB_DATABASE);

  $deviceID = $_POST["DeviceID"];
  $latitude = $_POST["Latitude"];
  $longitude = $_POST["Longitude"];
  $address = $_POST["Address"];
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
  if (empty($deviceID)) {
    die("Empty DeviceID");
    // prevents adding blank sales using this PHP link.
  }

  $query = "insert into Sales (DeviceID, Latitude, Longitude, Address, Title, Description, Image1, Image2, Image3, Email, PhoneNumber, PostedDateTime, StartDateTime, EndDateTime) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
  $stmt = $conn->prepare($query);
  $stmt->bind_param("ssssssssssssss", $deviceID, $latitude, $longitude, $address, $title, $description, $image1, $image2, $image3, $email, $phoneNumber, $postedDateTime, $startDateTime, $endDateTime);
  $stmt->execute();

  $query = "update Users set LastEndDateTime = ? where DeviceID = ?;";
  $stmt = $conn->prepare($query);
  $stmt->bind_param("ss", $endDateTime, $deviceID);
  $stmt->execute();

  // delete all sales that ended.
  $query = "delete from Sales where EndDateTime < ?;";
  $stmt = $conn->prepare($query);
  $stmt->bind_param("s", $postedDateTime);
  $stmt->execute();

  echo "New sale is added!";
  echo "Old sales are deleted!";
?>
