<?php
  include "/var/www/inc/dbinfo.inc";
  $conn = mysqli_connect(DB_SERVER, DB_USERNAME, DB_PASSWORD, DB_DATABASE);

  // use concat(DateTime). if not, milliseconds will not be considered by default.
  $query = "select DeviceID, Latitude, Longitude, concat(PostedDateTime), concat(EndDateTime), Title, Address from Sales;";
  $stmt = $conn->prepare($query);
  $stmt->bind_param();
  $stmt->execute();
  $result = $stmt->get_result() or die("Error: " .mysql_error());

  while($row = mysqli_fetch_array($result)) {
    for ($i = 0; $i < 7; $i++) {
      echo $row[$i];
      echo "MARK END COL";
    }
    echo "MARK END ROW";
  }
?>

