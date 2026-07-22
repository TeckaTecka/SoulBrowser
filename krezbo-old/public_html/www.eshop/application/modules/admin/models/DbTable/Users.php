<?php
class Admin_Model_DbTable_Users extends Zend_Db_Table_Abstract
{
	protected $_name = 'users';
	protected $_primary = 'id';
	/**
     * Return array of users
     * @return	array
     */
	public function getUsers($page)
	{
		$select = $this->select(Zend_Db_Table::SELECT_WITH_FROM_PART)
					   ->setIntegrityCheck(false);
		$select->join('users_types',
					'users_types.id = users.users_types_id', array('type'))
			->order('id DESC')
			->where('flags IS NULL')
			->limitPage($page, 20);
		$rows = $this->fetchAll($select);
		
		if (count($rows)==0) {
			return NULL;
		} else {
			$rows = $rows->toArray();
			return $rows;
		}
	}
	/**
     * Return array of user
     * @return	array
     */
	public function getUser($id)
	{
		$select = $this->select();
		$select->where('id = ?', $id);
		$row = $this->fetchRow($select);
		
		if (count($row)==0) {
			return NULL;
		} else {
			$row = $row->toArray();
			return $row;
		}
	}
	/**
     * set flag
	 * @param	int		$user_id
     * @param	string	$tag
     * @return	void
     */
	public function setFlag($user_id, $flag)
    {
    	$data = array('flags'	=>	$flag);
    	$this->update($data, 'id = '.$user_id);
    }
    /**
     * add user
	 * @param	int		$type_id
     * @param	string	$username
     * @param	string	$password
     * @param	string	$email
     * @param	string	$name
     * @param	string	$surname
     * @param	string	$date_of_birth
     * @param	boolean	$sex
     * @return	void
     */
	public function addUser($type_id, $username, $password, $email, $name, $surname, $date_of_birth, $sex)
    {
    	$data = array(
    		'users_types_id'	=>	$type_id,
    		'username'			=>	$username,
    		'password'			=>	$this->hashPassword($password),
    		'email'				=>	$email,
    		'name'				=>	($name)?$name:NULL,
    		'surname'			=>	($surname)?$surname:NULL,
			'date_of_birth'		=>	(is_null($date_of_birth))?NULL:$this->convertDate($date_of_birth),
			'sex'				=>	($sex=='')?NULL:$sex
    	);
    				 
        $this->insert($data);
    }
	/**
     * update user
     * @param	int		$id
	 * @param	int		$type_id
     * @param	string	$username
     * @param	string	$password
     * @param	string	$email
     * @param	string	$name
     * @param	string	$surname
     * @param	string	$date_of_birth
     * @param	boolean	$sex
     * @return	void
     */
	public function updateUser($id, $type_id, $username, $password, $email, $name, $surname, $date_of_birth, $sex)
    {
    	$data = array(
    		'users_types_id'	=>	$type_id,
    		'username'			=>	$username,
    		'email'				=>	$email,
    		'name'				=>	($name)?$name:NULL,
    		'surname'			=>	($surname)?$surname:NULL,
			'date_of_birth'		=>	(is_null($date_of_birth))?NULL:$this->convertDate($date_of_birth),
			'sex'				=>	($sex=='')?NULL:$sex
    	);
    	if ($password){
    		$data['password'] = $this->hashPassword($password);
    	}
    	$this->update($data, 'id = '.$id);
    }
    /**
     * hash password
	 * @param	string	$pwd
     * @return	string
     */
	protected function hashPassword($pwd)
	{
		return md5($pwd);
	}
	/**
     * convert date
	 * @param	string	$date
     * @return	string
     */
	protected function convertDate($date)
	{
		$date = new DateTime($date);
		//$date = DateTime::createFromFormat('d.m.Y', $date);
		return $date->format('Y-m-d');
	}
}