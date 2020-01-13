<?php
  include "/var/www/inc/dbinfo.inc";
  $conn = mysqli_connect(DB_SERVER, DB_USERNAME, DB_PASSWORD, DB_DATABASE);

  $deviceID = $_POST["DeviceID"];
  $isBuyer = $_POST["IsBuyer"];
  $antiques = $_POST["Antiques"];
  $bicycles = $_POST["Bicycles"];
  $books = $_POST["Books"];
  $clothes = $_POST["Clothes"];
  $exerciseEquipment = $_POST["ExerciseEquipment"];
  $games = $_POST["Games"];
  $jewelry = $_POST["Jewelry"];
  $outdoorFurniture = $_POST["OutdoorFurniture"];
  $tools = $_POST["Tools"];
  $toys = $_POST["Toys"];
  $others = $_POST["Others"];
  if (empty($deviceID)) {
    die("Empty DeviceId");
    // prevents deleting blank sales using this PHP link.
  }

  $query = "update Users set IsBuyer = ?, Antiques = ?, Bicycles = ?, Books = ?, Clothes = ?, ExerciseEquipment = ?, Games = ?, Jewelry = ?, OutdoorFurniture = ?, Tools = ?, Toys = ?, Others = ? where DeviceID = ?;";
  $stmt = $conn->prepare($query);
  $stmt->bind_param("sssssssssssss", $isBuyer, $antiques, $bicycles, $books, $clothes, $exerciseEquipment, $games, $jewelry, $outdoorFurniture, $tools, $toys, $others, $deviceID);
  $stmt->execute();

  echo "Interests are updated!";
?>
