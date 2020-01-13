<?php
  include "/var/www/inc/dbinfo.inc";
  $conn = mysqli_connect(DB_SERVER, DB_USERNAME, DB_PASSWORD, DB_DATABASE);

  $postedDateTime = $_POST["PostedDateTime"];

  $query = "select Address, Title, Description, Image1, Image2, Image3, Email, PhoneNumber, StartDateTime, EndDateTime from Sales where PostedDateTime = ? limit 1;";
  $stmt = $conn->prepare($query);
  $stmt->bind_param("s", $postedDateTime);
  $stmt->execute();
  $result = $stmt->get_result() or die("Error: " .mysql_error());

  $row = mysqli_fetch_row($result);
  for ($i = 0; $i < 10; $i++) {
    echo $row[$i];
    echo "MARK END COL";
  }
?>
