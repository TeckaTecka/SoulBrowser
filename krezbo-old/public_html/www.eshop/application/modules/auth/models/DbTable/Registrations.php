<?php
class Auth_Model_DbTable_Registrations extends Zend_Db_Table_Abstract
{
	protected $_name = 'registrations';
	protected $_primary = 'id';
	
	/**
     * Returns primary key
     * @param	string	$username
     * @param	string	$email
     * @return	int
     */
	public function setRegistration($username, $email)
	{
		$data = array('username'	=>	$username,
					  'email'		=>	$email,
    				  'token'		=>	$this->generateToken());
    	return $this->insert($data);    	
	}
	/**
     * Returns registration
     * @param	int	$id
     * @return	array
     */
	public function getRegistration($id)
	{
		$select = $this->select()
					   ->where('id = ?', $id);
		$row = $this->fetchRow($select);
				
		if ($row) {
			return $row->toArray();
		} else {
			return NULL;
		}
	}
	/**
     * Returns registration
     * @param	string	$token
     * @return	array
     */
	public function getRegistrationByToken($token)
	{
		$select = $this->select()
					   ->where('token = ?', $token);
		$row = $this->fetchRow($select);
				
		if ($row) {
			return $row->toArray();
		} else {
			return NULL;
		}
	}
	/**
     * Delete registration
     * @param	int	$id
     * @return	void
     */
	public function delRegistration($id) {
		$this->delete('id ='.(int)$id);
	}
	/**
     * Returns token
     * @param	int	$length = 32
     * @return	string
     */
	protected function generateToken($length=32)
	{
		$token = '';
		for ($i = 0; $i < $length; $i++) {
			$token .= chr(mt_rand(97, 122));
		}
    	return $token;    	
	}
}