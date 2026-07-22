<?php
class Auth_Model_DbTable_Users extends Zend_Db_Table_Abstract
{
	protected $_name = 'users';
	
	public function findCredentials($username, $pwd)
	{
		$select = $this->select(Zend_Db_Table::SELECT_WITH_FROM_PART)
					   ->setIntegrityCheck(false);
		$select->join('users_types',
		       		  'users.users_types_id = users_types.id', 'type')
		 	   ->where('username = ?',$username);
		if ($pwd){
			$select->where('password = ?', $this->hashPassword($pwd));
		}			   
		$row = $this->fetchRow($select);
		if($row)
		{
			return $row;
		}
		return false;
	}
	public function getUser($id)
	{
		$select = $this->select(Zend_Db_Table::SELECT_WITH_FROM_PART)
					   ->setIntegrityCheck(false);
		$select->join('users_types',
		       		  'users.users_types_id = users_types.id', 'type')
		 	   ->where('users.id = ?',$id);
		$row = $this->fetchRow($select);
		if($row)
		{
			return $row;
		}
		return false;
	}
	protected function hashPassword($pwd)
	{
		return md5($pwd);
	}
	public function isUser($username)
	{
		$select = $this->select()->where('username = ?',$username);
		$row = $this->fetchRow($select);
		if($row)
		{
			return true;
		}
		return false;
	}
	public function addUser($username, $password, $email)
    {
    	$data = array('username' => $username,
    				  'password' => $this->hashPassword($password),
    				  'email' => $email);
    				 
        $this->insert($data);
    }
	public function getUserByEmail($email) {
		$select = $this->select(Zend_Db_Table::SELECT_WITH_FROM_PART)
					   ->setIntegrityCheck(false);
		$select->join('users_types',
		       		  'users.users_types_id = users_types.id', 'type')
			   ->where('users.email = ?', $email);
		
		$row = $this->fetchRow($select);
				
		if (count($row)==0) {
			return NULL;
		} else {
			return $row->toArray();
		}	
	}
	public function genetarePassword($length = 6){
		$characters = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';
	    $string = '';    
	
	    for ($p = 0; $p < $length; $p++) {
	        $string .= $characters[mt_rand(0, strlen($characters)-1)];
	    }
		
	    return $string;
	}
	public function updateUser($id, $username, $password, $email)
	{
		$data = array('username' => $username,
    				  'password' => $this->hashPassword($password),
    				  'email' => $email);
		
		$this->update($data, 'id = '.$id);
	}
	public function updateUserInfo($id, $email, $name, $surname, $dateOfBirth, $sex)
	{
		$data = array('email'			=>	$email,
    				  'name'			=>	($name)?$name:NULL,
    				  'surname'			=>	($surname)?$surname:NULL,
					  'date_of_birth'	=>	($dateOfBirth<>'')?$this->convertDate($dateOfBirth):NULL,
					  'sex'				=>	($sex<>'')?$sex:NULL);
		
		$this->update($data, 'id = '.$id);
	}
	protected function convertDate($date)
	{
		$date = new DateTime($date);
		//$date = DateTime::createFromFormat('d.m.Y', $date);
		return $date->format('Y-m-d');
	}
}