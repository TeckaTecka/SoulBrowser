<?php
class Newsletter_Model_DbTable_NewslettersEmails extends Zend_Db_Table_Abstract
{
	protected $_name = 'newsletters_emails';
	protected $_primary = 'id';
	
	/**
     * Returns primary key
     * @param	string	$email
     * @return	int
     */
	public function setEmail($email)
	{
		$data = array('email'		=>	$email);
    	return $this->insert($data);    	
	}
	/**
     * Return array of newsletters_emails
     * @param int	$page
     * @return	array
     */
	public function getEmails($page)
	{
		$select = $this->select();
		$select
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
     * Return array of email
     * @param int	$id
     * @return	array
     */
	public function getEmail($id)
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
     * Return all of newsletters_emails
     * @return	array
     */
	public function getAllEmails()
	{
		$select = $this->select();
		$select
			->where('flags IS NULL');
		$rows = $this->fetchAll($select);
		
		if (count($rows)==0) {
			return NULL;
		} else {
			$rows = $rows->toArray();
			return $rows;
		}
	}
	/**
     * set flag
	 * @param	int		$id
     * @param	string	$tag
     * @return	void
     */
	public function setFlag($id, $flag)
    {
    	$data = array('flags'	=>	$flag);
    	$this->update($data, 'id = '.$id);
    }
}